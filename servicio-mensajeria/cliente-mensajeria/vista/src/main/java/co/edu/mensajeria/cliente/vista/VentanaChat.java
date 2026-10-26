package co.edu.mensajeria.cliente.vista;

import co.edu.mensajeria.cliente.dto.MensajeChat;
import co.edu.mensajeria.cliente.dto.UsuarioRemoto;
import co.edu.mensajeria.cliente.fachada.IFachadaCliente;
import co.edu.mensajeria.cliente.fachada.ObservadorCliente;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Ventana principal: contactos a la izquierda y la conversación elegida a la derecha.
 *
 * <p>Es observadora de la fachada: lo que llega del servidor entra por los tres métodos de
 * {@link ObservadorCliente}, que pasan el trabajo al hilo de Swing. Lo que habla con el
 * servidor se ejecuta en {@link SegundoPlano} para no congelar la ventana.</p>
 */
public class VentanaChat extends JFrame implements ObservadorCliente {

    private static final Set<String> EXTENSIONES_IMAGEN = Set.of("png", "jpg", "jpeg", "gif", "bmp");

    private final IFachadaCliente fachada;
    private final Runnable alCerrarSesion;
    private final DefaultListModel<UsuarioRemoto> usuarios = new DefaultListModel<>();
    private final DefaultListModel<MensajeChat> conversacion = new DefaultListModel<>();
    private final Set<String> noLeidos = new HashSet<>();
    private final SegundoPlano segundoPlano = new SegundoPlano("peticiones-servidor");
    private final PanelesChat paneles;
    private String contactoActual;

    /** @param alCerrarSesion qué hacer después de que el usuario cierra sesión (volver al inicio de sesión) */
    public VentanaChat(IFachadaCliente fachada, Runnable alCerrarSesion) {
        super("Mensajería académica - " + fachada.usuarioActual().nombreCompleto()
                + " (" + fachada.usuarioActual().codigo() + ")");
        this.fachada = fachada;
        this.alCerrarSesion = alCerrarSesion;
        this.paneles = new PanelesChat(usuarios, conversacion, noLeidos::contains,
                new RenderizadorMensaje(this::nombreDe));
        setContentPane(paneles.raiz(fachada.usuarioActual()));
        conectarEventos();
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(980, 660);
        setLocationRelativeTo(null);
        fachada.agregarObservador(this);
        cargarUsuarios();
    }

    @Override
    public void mensajeRecibido(MensajeChat m) {
        SwingUtilities.invokeLater(() -> {
            if (m.contacto().equals(contactoActual)) {
                agregarAConversacion(m);
            } else {
                noLeidos.add(m.contacto());
                paneles.listaUsuarios().repaint();
            }
            paneles.estado("Mensaje nuevo de " + nombreDe(m.origen()));
        });
    }

    @Override
    public void presenciaCambiada(String codigo, boolean conectado) {
        SwingUtilities.invokeLater(() -> {
            int i = indiceDe(codigo);
            if (i < 0) {
                cargarUsuarios(); // un usuario que no estaba en la lista
                return;
            }
            usuarios.set(i, usuarios.get(i).conConexion(conectado));
            paneles.estado(nombreDe(codigo) + (conectado ? " se conectó" : " se desconectó"));
            if (codigo.equals(contactoActual)) {
                paneles.cabecera(usuarios.get(i));
            }
        });
    }

    @Override
    public void conexionCerrada(String motivo) {
        SwingUtilities.invokeLater(() -> {
            paneles.habilitarEnvio(false);
            paneles.estado("Sin conexión: " + motivo);
            Object[] opciones = {"Iniciar sesión de nuevo", "Salir"};
            int opcion = JOptionPane.showOptionDialog(this, "La sesión terminó: " + motivo, "Sesión cerrada",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, opciones, opciones[0]);
            terminar(opcion == 0);
        });
    }

    private void conectarEventos() {
        paneles.listaUsuarios().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                abrirConversacion();
            }
        });
        paneles.alEnviar(this::enviarTexto);
        paneles.alAdjuntar(this::enviarArchivo);
        paneles.alCerrarSesion(() -> salir(true));
        paneles.listaMensajes().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int i = paneles.listaMensajes().locationToIndex(e.getPoint());
                if (e.getClickCount() == 2 && i >= 0) {
                    abrirArchivo(conversacion.get(i));
                }
            }
        });
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                salir(false);
            }
        });
    }

    private void cargarUsuarios() {
        paneles.estado("Cargando usuarios…");
        segundoPlano.ejecutar(fachada::listarUsuarios, lista -> {
            String elegido = contactoActual;
            usuarios.clear();
            usuarios.addElement(UsuarioRemoto.todos());
            String yo = fachada.usuarioActual().codigo();
            lista.stream().filter(u -> !u.codigo().equals(yo)).forEach(usuarios::addElement);
            int i = elegido == null ? 0 : Math.max(0, indiceDe(elegido));
            paneles.listaUsuarios().setSelectedIndex(i);
            long conectados = lista.stream().filter(UsuarioRemoto::conectado).count();
            paneles.estado("Usuarios registrados: " + lista.size() + " · conectados: " + conectados);
        }, motivo -> paneles.estado("No se pudo cargar la lista de usuarios: " + motivo));
    }

    private void abrirConversacion() {
        UsuarioRemoto contacto = paneles.listaUsuarios().getSelectedValue();
        if (contacto == null || contacto.codigo().equals(contactoActual)) {
            return;
        }
        contactoActual = contacto.codigo();
        noLeidos.remove(contactoActual);
        paneles.cabecera(contacto);
        conversacion.clear();
        try {
            fachada.conversacion(contactoActual).forEach(conversacion::addElement);
        } catch (RuntimeException e) {
            paneles.estado("No se pudo leer el historial: " + SegundoPlano.motivo(e));
        }
        paneles.irAlFinal();
        paneles.listaUsuarios().repaint();
        paneles.enfocarTexto();
    }

    private void enviarTexto() {
        String texto = paneles.textoEscrito().strip();
        String destino = contactoActual;
        if (texto.isEmpty() || destino == null) {
            return;
        }
        paneles.limpiarTexto();
        segundoPlano.ejecutar(() -> fachada.enviarTexto(destino, texto), this::agregarSiSigueAbierta,
                motivo -> mostrarError("No se pudo enviar el mensaje", motivo));
    }

    private void enviarArchivo() {
        String destino = contactoActual;
        JFileChooser selector = new JFileChooser();
        if (destino == null || selector.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path archivo = selector.getSelectedFile().toPath();
        Optional<List<String>> filtros = esImagen(archivo)
                ? SelectorFiltros.elegir(this, archivo.getFileName().toString(), fachada.filtrosDisponibles())
                : Optional.of(List.of());
        if (filtros.isEmpty()) {
            return;
        }
        paneles.estado("Enviando " + archivo.getFileName() + "…");
        segundoPlano.ejecutar(() -> fachada.enviarArchivo(destino, archivo, filtros.get()), m -> {
            agregarSiSigueAbierta(m);
            paneles.estado("Se envió " + m.contenido());
        }, motivo -> mostrarError("No se pudo enviar " + archivo.getFileName(), motivo));
    }

    private void abrirArchivo(MensajeChat m) {
        if (m.rutaArchivo() == null) {
            return;
        }
        File archivo = new File(m.rutaArchivo());
        if (!archivo.isFile()) {
            mostrarError("No se encuentra el archivo", m.rutaArchivo());
        } else if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            segundoPlano.ejecutar(() -> abrirConElSistema(archivo), ok -> { },
                    motivo -> mostrarError("No se pudo abrir " + archivo.getName(), motivo));
        } else {
            JOptionPane.showMessageDialog(this, "El archivo está en:\n" + archivo.getAbsolutePath());
        }
    }

    private static boolean abrirConElSistema(File archivo) throws IOException {
        Desktop.getDesktop().open(archivo);
        return true;
    }

    private void agregarSiSigueAbierta(MensajeChat m) {
        if (m.contacto().equals(contactoActual)) {
            agregarAConversacion(m);
        }
    }

    /** Un mensaje enviado a uno mismo llega también como notificación: no se repite. */
    private void agregarAConversacion(MensajeChat m) {
        for (int i = 0; i < conversacion.size(); i++) {
            if (conversacion.get(i).id().equals(m.id())) {
                return;
            }
        }
        conversacion.addElement(m);
        paneles.irAlFinal();
    }

    /** Envía SALIR en otro hilo, para que la ventana no se congele si el servidor no responde. */
    private void salir(boolean volverAlInicio) {
        setVisible(false);
        Thread hilo = new Thread(() -> {
            fachada.cerrarSesion();
            SwingUtilities.invokeLater(() -> terminar(volverAlInicio));
        }, "cerrar-sesion");
        hilo.setDaemon(true);
        hilo.start();
    }

    private void terminar(boolean volverAlInicio) {
        segundoPlano.detener();
        dispose();
        if (volverAlInicio) {
            alCerrarSesion.run();
        } else {
            System.exit(0);
        }
    }

    private void mostrarError(String titulo, String motivo) {
        paneles.estado(titulo + ": " + motivo);
        JOptionPane.showMessageDialog(this, motivo, titulo, JOptionPane.ERROR_MESSAGE);
    }

    private int indiceDe(String codigo) {
        for (int i = 0; i < usuarios.size(); i++) {
            if (usuarios.get(i).codigo().equals(codigo)) {
                return i;
            }
        }
        return -1;
    }

    private String nombreDe(String codigo) {
        if (RenderizadorMensaje.SERVIDOR.equals(codigo)) {
            return "Servidor";
        }
        int i = indiceDe(codigo);
        return i < 0 ? codigo : usuarios.get(i).nombreCompleto();
    }

    static boolean esImagen(Path archivo) {
        String nombre = archivo.getFileName().toString().toLowerCase(Locale.ROOT);
        int punto = nombre.lastIndexOf('.');
        return punto >= 0 && EXTENSIONES_IMAGEN.contains(nombre.substring(punto + 1));
    }
}
