package co.edu.mensajeria.cliente.soporte;

import co.edu.mensajeria.cliente.comunicacion.Protocolo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Servidor mínimo que habla el protocolo TCP del servidor real, para probar el cliente
 * completo sin depender de él: LOGIN (clave = "clave" + código), USUARIOS, TEXTO, ARCHIVO
 * y SALIR, con las notificaciones MENSAJE, PRESENCIA y CIERRE. Guarda cada línea recibida.
 */
public final class ServidorDePrueba implements AutoCloseable {

    public static final Map<String, String> NOMBRES = Map.of(
            "1001", "Ana|Gómez|Sistemas", "1002", "Carlos|Pérez|Sistemas", "1003", "Laura|Martínez|Matemáticas");
    private static final List<String> CODIGOS = List.of("1001", "1002", "1003");

    /** Todas las líneas que enviaron los clientes, en orden. */
    public final BlockingQueue<String> recibidas = new LinkedBlockingQueue<>();
    private final ServerSocket servidor;
    private final List<Sesion> sesiones = new CopyOnWriteArrayList<>();
    private final ExecutorService hilos = Executors.newCachedThreadPool();

    public ServidorDePrueba() throws IOException {
        servidor = new ServerSocket(0);
        hilos.submit(this::aceptar);
    }

    public int puerto() {
        return servidor.getLocalPort();
    }

    /** Mensaje del administrador a todos ({@code origen = SERVIDOR}). */
    public void difundir(String texto) {
        String linea = notificacionMensaje(UUID.randomUUID().toString(), "SERVIDOR", "*", "TEXTO", texto, "", "");
        sesiones.stream().filter(s -> s.codigo != null).forEach(s -> s.enviar(linea));
    }

    /** Como "cerrar-usuario" en la consola del servidor: avisa el cierre y corta. */
    public void cerrarSesionesDe(String codigo, String motivo) {
        for (Sesion s : sesiones) {
            if (codigo.equals(s.codigo)) {
                s.enviar(Protocolo.unir("NOTIF", "CIERRE", motivo));
                s.cerrar();
            }
        }
    }

    /** Corta todas las conexiones sin avisar, como si el servidor se cayera. */
    public void caer() {
        sesiones.forEach(Sesion::cerrar);
    }

    @Override
    public void close() throws IOException {
        servidor.close();
        caer();
        hilos.shutdownNow();
    }

    private void aceptar() {
        while (!servidor.isClosed()) {
            try {
                Sesion s = new Sesion(servidor.accept());
                sesiones.add(s);
                hilos.submit(() -> atender(s));
            } catch (IOException e) {
                return;
            }
        }
    }

    private void atender(Sesion s) {
        try (BufferedReader entrada = new BufferedReader(
                new InputStreamReader(s.socket.getInputStream(), StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = entrada.readLine()) != null && !s.socket.isClosed()) {
                recibidas.add(linea);
                if (!responder(s, Protocolo.separar(linea))) {
                    break;
                }
            }
        } catch (IOException e) {
            // el cliente o la prueba cerraron la conexión
        } finally {
            s.cerrar();
            sesiones.remove(s);
            if (s.codigo != null && sesiones.stream().noneMatch(o -> s.codigo.equals(o.codigo))) {
                presencia(s.codigo, false);
            }
        }
    }

    /** Responde una petición. Devuelve {@code false} si la sesión termina. */
    private boolean responder(Sesion s, List<String> c) {
        switch (c.get(0)) {
            case "LOGIN" -> login(s, c.get(1), c.get(2));
            case "USUARIOS" -> s.enviar(usuarios());
            case "TEXTO" -> reenviar(s, c.get(1), "TEXTO", c.get(2), "mensaje.txt", "");
            case "ARCHIVO" -> reenviar(s, c.get(1), c.get(2).endsWith(".png") ? "IMAGEN" : "ARCHIVO", "", c.get(2),
                    c.get(3));
            case "SALIR" -> {
                s.enviar("OK");
                return false;
            }
            default -> s.enviar("ERROR|comando desconocido");
        }
        return true;
    }

    private void login(Sesion s, String codigo, String clave) {
        if (!NOMBRES.containsKey(codigo) || !("clave" + codigo).equals(clave)) {
            s.enviar("ERROR|credenciales inválidas");
            return;
        }
        boolean primera = sesiones.stream().noneMatch(o -> codigo.equals(o.codigo));
        s.codigo = codigo;
        s.enviar("OK|" + codigo + "|" + NOMBRES.get(codigo));
        if (primera) {
            presencia(codigo, true);
        }
    }

    private String usuarios() {
        StringBuilder sb = new StringBuilder("OK|" + CODIGOS.size());
        for (String codigo : CODIGOS) {
            boolean conectado = sesiones.stream().anyMatch(o -> codigo.equals(o.codigo));
            sb.append('|').append(codigo).append('|').append(NOMBRES.get(codigo)).append('|')
                    .append(conectado ? "1" : "0");
        }
        return sb.toString();
    }

    /** Responde OK|id al remitente y después entrega la notificación, como el servidor real. */
    private void reenviar(Sesion origen, String destino, String tipo, String contenido, String nombre, String base64) {
        if (origen.codigo == null) {
            origen.enviar("ERROR|debe iniciar sesión");
            return;
        }
        String id = UUID.randomUUID().toString();
        origen.enviar(Protocolo.unir("OK", id));
        String linea = notificacionMensaje(id, origen.codigo, destino, tipo, contenido, nombre, base64);
        for (Sesion s : sesiones) {
            boolean paraTodos = "*".equals(destino) && s.codigo != null && !s.codigo.equals(origen.codigo);
            if (paraTodos || destino.equals(s.codigo)) {
                s.enviar(linea);
            }
        }
    }

    private void presencia(String codigo, boolean conectado) {
        String linea = Protocolo.unir("NOTIF", "PRESENCIA", codigo, conectado ? "1" : "0");
        sesiones.stream().filter(s -> s.codigo != null && !codigo.equals(s.codigo)).forEach(s -> s.enviar(linea));
    }

    private static String notificacionMensaje(String id, String origen, String destino, String tipo,
                                              String contenido, String nombre, String base64) {
        return Protocolo.unir("NOTIF", "MENSAJE", id, origen, destino, tipo, LocalDateTime.now().toString(),
                contenido, nombre, base64);
    }

    /** Una conexión de un cliente. */
    private static final class Sesion {
        final Socket socket;
        final PrintWriter salida;
        volatile String codigo;

        Sesion(Socket socket) throws IOException {
            this.socket = socket;
            this.salida = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        }

        synchronized void enviar(String linea) {
            salida.print(linea + "\n");
            salida.flush();
        }

        void cerrar() {
            try {
                socket.close();
            } catch (IOException ignorada) {
                // ya estaba cerrada
            }
        }
    }
}
