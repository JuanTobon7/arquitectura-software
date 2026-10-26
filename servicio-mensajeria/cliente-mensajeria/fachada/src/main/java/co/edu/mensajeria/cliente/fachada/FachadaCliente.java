package co.edu.mensajeria.cliente.fachada;

import co.edu.mensajeria.cliente.comunicacion.IConexionServidor;
import co.edu.mensajeria.cliente.dto.MensajeChat;
import co.edu.mensajeria.cliente.dto.TipoMensaje;
import co.edu.mensajeria.cliente.dto.UsuarioRemoto;
import co.edu.mensajeria.cliente.persistencia.IRepositorioMensajesLocal;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Casos de uso del cliente (GRASP Controlador). Traduce cada acción de la vista a una
 * petición del protocolo, guarda el historial y reparte a los observadores lo que llega
 * del servidor. Solo conoce interfaces: la conexión y el repositorio llegan por constructor.
 */
public class FachadaCliente implements IFachadaCliente {

    static final String LOGIN = "LOGIN";
    static final String USUARIOS = "USUARIOS";
    static final String TEXTO = "TEXTO";
    static final String ARCHIVO = "ARCHIVO";
    static final String SALIR = "SALIR";
    private static final int CAMPOS_POR_USUARIO = 5;

    private final IConexionServidor conexion;
    private final IRepositorioMensajesLocal repositorio;
    private final Path directorioDescargas;
    private final List<String> filtrosDisponibles;
    private final int tamanoMaximoBytes;
    private final List<ObservadorCliente> observadores = new CopyOnWriteArrayList<>();
    private final AtomicBoolean cierreAvisado = new AtomicBoolean();
    /** Un mensaje que llega justo después del LOGIN espera a que se conozca el usuario. */
    private final Object sesion = new Object();
    private volatile UsuarioRemoto usuario;

    public FachadaCliente(IConexionServidor conexion, IRepositorioMensajesLocal repositorio, Path directorioDescargas,
                          List<String> filtrosDisponibles, int tamanoMaximoBytes) {
        this.conexion = conexion;
        this.repositorio = repositorio;
        this.directorioDescargas = directorioDescargas;
        this.filtrosDisponibles = List.copyOf(filtrosDisponibles);
        this.tamanoMaximoBytes = tamanoMaximoBytes;
    }

    @Override
    public UsuarioRemoto iniciarSesion(String host, int puerto, String codigo, String clave) throws IOException {
        synchronized (sesion) {
            conexion.alRecibirNotificacion(this::atenderNotificacion);
            conexion.conectar(host, puerto);
            try {
                List<String> perfil = conexion.pedir(LOGIN, codigo, clave);
                usuario = new UsuarioRemoto(campo(perfil, 0), campo(perfil, 1), campo(perfil, 2), campo(perfil, 3),
                        true);
                cierreAvisado.set(false);
                return usuario;
            } catch (IOException | RuntimeException e) {
                conexion.cerrar();
                throw e;
            }
        }
    }

    @Override
    public UsuarioRemoto usuarioActual() {
        return usuario;
    }

    @Override
    public List<UsuarioRemoto> listarUsuarios() throws IOException {
        List<String> r = conexion.pedir(USUARIOS);
        int total;
        try {
            total = Integer.parseInt(campo(r, 0));
        } catch (NumberFormatException e) {
            throw new IOException("respuesta inválida del servidor a USUARIOS", e);
        }
        List<UsuarioRemoto> usuarios = new ArrayList<>();
        for (int i = 0; i < total; i++) {
            int base = 1 + i * CAMPOS_POR_USUARIO;
            usuarios.add(new UsuarioRemoto(campo(r, base), campo(r, base + 1), campo(r, base + 2),
                    campo(r, base + 3), "1".equals(campo(r, base + 4))));
        }
        return usuarios;
    }

    @Override
    public MensajeChat enviarTexto(String destino, String texto) throws IOException {
        String yo = propietario();
        String id = campo(conexion.pedir(TEXTO, destino, texto), 0);
        MensajeChat m = new MensajeChat(id, yo, destino, yo, destino, TipoMensaje.TEXTO, ahora(), texto, null);
        guardarEnHistorial(m);
        return m;
    }

    @Override
    public MensajeChat enviarArchivo(String destino, Path archivo, List<String> filtros) throws IOException {
        String yo = propietario();
        validarTamano(archivo);
        byte[] datos = Files.readAllBytes(archivo);
        String nombre = archivo.getFileName().toString();
        String base64 = Base64.getEncoder().encodeToString(datos);
        List<String> respuesta = filtros.isEmpty()
                ? conexion.pedir(ARCHIVO, destino, nombre, base64)
                : conexion.pedir(ARCHIVO, destino, nombre, base64, String.join(",", filtros));
        String id = campo(respuesta, 0);
        // Se guarda una copia: el historial no depende de que el original siga en su lugar.
        Path copia = guardarDescarga(yo, id, nombre, datos);
        TipoMensaje tipo = esImagen(datos) ? TipoMensaje.IMAGEN : TipoMensaje.ARCHIVO;
        MensajeChat m = new MensajeChat(id, yo, destino, yo, destino, tipo, ahora(), nombre, copia.toString());
        guardarEnHistorial(m);
        return m;
    }

    @Override
    public List<MensajeChat> conversacion(String contacto) {
        return repositorio.conversacion(propietario(), contacto);
    }

    @Override
    public List<String> filtrosDisponibles() {
        return filtrosDisponibles;
    }

    @Override
    public void agregarObservador(ObservadorCliente o) {
        observadores.add(o);
    }

    @Override
    public void cerrarSesion() {
        cierreAvisado.set(true); // lo pidió el usuario: no hay que avisarle
        if (conexion.conectada()) {
            try {
                conexion.pedir(SALIR);
            } catch (IOException ignorada) {
                // Se cierra igual: el servidor limpia la sesión al ver el socket cerrado.
            }
        }
        conexion.cerrar();
    }

    /** Escucha de la conexión: recibe {@code [tipo, datos...]} en el hilo de notificaciones. */
    void atenderNotificacion(List<String> campos) {
        switch (campos.get(0)) {
            case IConexionServidor.NOTIF_MENSAJE -> avisarMensaje(campos);
            case IConexionServidor.NOTIF_PRESENCIA -> {
                boolean conectado = "1".equals(campo(campos, 2));
                observadores.forEach(o -> o.presenciaCambiada(campo(campos, 1), conectado));
            }
            case IConexionServidor.NOTIF_CIERRE -> avisarCierre(campo(campos, 1));
            default -> {
                // Notificación de una versión más nueva del servidor: se ignora.
            }
        }
    }

    private void avisarMensaje(List<String> campos) {
        MensajeChat m;
        try {
            m = recibirMensaje(campos);
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("No se pudo recibir el mensaje " + campo(campos, 1) + ": " + e.getMessage());
            return;
        }
        guardarEnHistorial(m);
        observadores.forEach(o -> o.mensajeRecibido(m));
    }

    private void avisarCierre(String motivo) {
        if (cierreAvisado.compareAndSet(false, true)) {
            observadores.forEach(o -> o.conexionCerrada(motivo));
        }
        conexion.cerrar();
    }

    /**
     * {@code [MENSAJE, id, origen, destino, tipo, fecha, contenido, nombreArchivo, base64]} → mensaje.
     * Si trae archivo, lo guarda en las descargas.
     */
    MensajeChat recibirMensaje(List<String> campos) throws IOException {
        String yo;
        synchronized (sesion) {
            yo = propietario();
        }
        String id = campo(campos, 1);
        String origen = campo(campos, 2);
        String destino = campo(campos, 3);
        TipoMensaje tipo = TipoMensaje.valueOf(campo(campos, 4));
        LocalDateTime fecha = fecha(campo(campos, 5));
        String contacto = contactoDe(yo, origen, destino);
        if (tipo == TipoMensaje.TEXTO) {
            return new MensajeChat(id, yo, contacto, origen, destino, tipo, fecha, campo(campos, 6), null);
        }
        String nombre = campo(campos, 7);
        Path ruta = guardarDescarga(yo, id, nombre, Base64.getDecoder().decode(campo(campos, 8)));
        return new MensajeChat(id, yo, contacto, origen, destino, tipo, fecha, nombre, ruta.toString());
    }

    /**
     * Guarda los bytes en {@code descargas/<propietario>/<id>_<nombre>}. El nombre se sanea
     * para que un remitente no pueda escribir fuera de la carpeta (por ejemplo con {@code ../}).
     */
    Path guardarDescarga(String propietario, String id, String nombre, byte[] datos) throws IOException {
        Path carpeta = directorioDescargas.resolve(nombreSeguro(propietario)).toAbsolutePath();
        Files.createDirectories(carpeta);
        Path archivo = carpeta.resolve(nombreSeguro(id + "_" + nombre));
        Files.write(archivo, datos);
        return archivo;
    }

    /** Conversación a la que pertenece un mensaje: la de todos, o la del otro usuario. */
    static String contactoDe(String yo, String origen, String destino) {
        if (UsuarioRemoto.TODOS.equals(destino)) {
            return UsuarioRemoto.TODOS;
        }
        return yo.equals(origen) ? destino : origen;
    }

    /** Detecta una imagen por su cabecera, igual que el servidor. */
    static boolean esImagen(byte[] datos) {
        try (ImageInputStream entrada = ImageIO.createImageInputStream(new ByteArrayInputStream(datos))) {
            return entrada != null && ImageIO.getImageReaders(entrada).hasNext();
        } catch (IOException e) {
            return false;
        }
    }

    static String nombreSeguro(String nombre) {
        String limpio = nombre.replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "_").strip();
        return limpio.isEmpty() || limpio.matches("\\.+") ? "archivo" : limpio;
    }

    private void validarTamano(Path archivo) throws IOException {
        long tamano = Files.size(archivo);
        if (tamano == 0) {
            throw new IOException("el archivo está vacío");
        }
        if (tamano > tamanoMaximoBytes) {
            throw new IOException("el archivo pesa " + megas(tamano) + " MB y el máximo es " + megas(tamanoMaximoBytes)
                    + " MB");
        }
    }

    private void guardarEnHistorial(MensajeChat m) {
        try {
            repositorio.guardar(m);
        } catch (RuntimeException e) {
            // El mensaje ya viajó: se muestra aunque el historial falle.
            System.err.println("No se pudo guardar en el historial: " + e.getMessage());
        }
    }

    private String propietario() {
        UsuarioRemoto u = usuario;
        if (u == null) {
            throw new IllegalStateException("primero hay que iniciar sesión");
        }
        return u.codigo();
    }

    private static LocalDateTime fecha(String texto) {
        try {
            return LocalDateTime.parse(texto).truncatedTo(ChronoUnit.MILLIS);
        } catch (DateTimeParseException e) {
            return ahora();
        }
    }

    private static LocalDateTime ahora() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
    }

    private static String megas(long bytes) {
        return String.format("%.1f", bytes / (1024.0 * 1024.0));
    }

    private static String campo(List<String> campos, int i) {
        return i < campos.size() ? campos.get(i) : "";
    }
}
