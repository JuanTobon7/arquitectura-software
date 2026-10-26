package co.edu.mensajeria.cliente.comunicacion;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Conexión TCP con el servidor.
 *
 * <p>Un hilo lector recibe todas las líneas. Las respuestas ({@code OK}/{@code ERROR}) van a
 * la cola {@code respuestas}, donde las espera {@link #pedir(String...)}; las notificaciones
 * ({@code NOTIF}) se entregan a la escucha en otro hilo, para que una escucha lenta no
 * retrase las respuestas. Cada conexión tiene su propia cola, así que un lector viejo no
 * puede dejar nada en la de una conexión nueva.</p>
 */
public class ConexionServidorTCP implements IConexionServidor {

    static final Duration ESPERA_POR_DEFECTO = Duration.ofSeconds(30);
    static final String MOTIVO_CONEXION_PERDIDA = "se perdió la conexión con el servidor";
    private static final int ESPERA_CONEXION_MS = 5000;
    /** Lo deja el lector en la cola cuando la conexión termina: despierta a quien espera una respuesta. */
    private static final List<String> FIN = List.of();

    private final Duration espera;
    private final Object turno = new Object();
    private final AtomicReference<Socket> socket = new AtomicReference<>();
    private volatile BlockingQueue<List<String>> respuestas = new LinkedBlockingQueue<>();
    private volatile Consumer<List<String>> escucha = campos -> { };
    private volatile PrintWriter salida;
    private volatile ExecutorService notificaciones;

    public ConexionServidorTCP() {
        this(ESPERA_POR_DEFECTO);
    }

    /** @param espera cuánto se espera la respuesta a una petición antes de dar la conexión por perdida */
    public ConexionServidorTCP(Duration espera) {
        this.espera = espera;
    }

    @Override
    public void conectar(String host, int puerto) throws IOException {
        cerrar();
        Socket s = new Socket();
        try {
            s.connect(new InetSocketAddress(host, puerto), ESPERA_CONEXION_MS);
        } catch (IOException e) {
            s.close();
            throw new IOException("no se pudo conectar con " + host + ":" + puerto + " (" + e.getMessage() + ")", e);
        }
        BlockingQueue<List<String>> cola = new LinkedBlockingQueue<>();
        BufferedReader entrada = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
        salida = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8)));
        respuestas = cola;
        notificaciones = Executors.newSingleThreadExecutor(r -> hiloDemonio(r, "notificaciones-servidor"));
        socket.set(s);
        hiloDemonio(() -> leer(s, entrada, cola), "lector-servidor").start();
    }

    @Override
    public List<String> pedir(String... campos) throws IOException {
        synchronized (turno) {
            PrintWriter out = salida;
            if (!conectada() || out == null) {
                throw new IOException("no hay conexión con el servidor");
            }
            BlockingQueue<List<String>> cola = respuestas;
            out.print(Protocolo.unir(campos));
            out.print('\n');
            out.flush();
            if (out.checkError()) {
                throw new IOException("no se pudo enviar la petición al servidor");
            }
            return interpretar(esperarRespuesta(cola));
        }
    }

    @Override
    public void alRecibirNotificacion(Consumer<List<String>> escucha) {
        this.escucha = escucha == null ? campos -> { } : escucha;
    }

    @Override
    public boolean conectada() {
        Socket s = socket.get();
        return s != null && !s.isClosed();
    }

    @Override
    public void cerrar() {
        Socket s = socket.getAndSet(null);
        cerrarSocket(s);
        ExecutorService n = notificaciones;
        if (n != null) {
            n.shutdown();
        }
    }

    /** Hilo lector: reparte cada línea hasta que la conexión termina. */
    private void leer(Socket s, BufferedReader entrada, BlockingQueue<List<String>> cola) {
        try {
            String linea;
            while ((linea = entrada.readLine()) != null) {
                if (!linea.isEmpty()) {
                    entregar(Protocolo.separar(linea), cola);
                }
            }
        } catch (IOException e) {
            // Socket cerrado con cerrar() o conexión interrumpida: se resuelve abajo.
        }
        cola.offer(FIN);
        if (socket.compareAndSet(s, null)) {
            // Nadie llamó a cerrar(): el servidor cortó la conexión.
            cerrarSocket(s);
            notificar(List.of(NOTIF_CIERRE, MOTIVO_CONEXION_PERDIDA));
            notificaciones.shutdown();
        }
    }

    private void entregar(List<String> campos, BlockingQueue<List<String>> cola) {
        if (Protocolo.NOTIFICACION.equals(campos.get(0))) {
            notificar(campos.subList(1, campos.size()));
        } else {
            cola.offer(campos);
        }
    }

    private void notificar(List<String> campos) {
        List<String> copia = List.copyOf(campos);
        Consumer<List<String>> destino = escucha;
        try {
            notificaciones.execute(() -> {
                try {
                    destino.accept(copia);
                } catch (RuntimeException e) {
                    System.err.println("Error atendiendo la notificación " + copia.get(0) + ": " + e);
                }
            });
        } catch (RejectedExecutionException e) {
            // La conexión ya se cerró con cerrar(): la notificación se descarta.
        }
    }

    private List<String> esperarRespuesta(BlockingQueue<List<String>> cola) throws IOException {
        List<String> respuesta;
        try {
            respuesta = cola.poll(espera.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InterruptedIOException("se interrumpió la espera de la respuesta del servidor");
        }
        if (respuesta == null) {
            // Ya no se sabe a qué petición corresponde la próxima respuesta: se corta la conexión.
            cerrar();
            throw new IOException("el servidor no respondió a tiempo");
        }
        if (respuesta == FIN) {
            throw new IOException(MOTIVO_CONEXION_PERDIDA);
        }
        return respuesta;
    }

    private static List<String> interpretar(List<String> respuesta) throws IOException {
        String tipo = respuesta.get(0);
        if (Protocolo.OK.equals(tipo)) {
            return List.copyOf(respuesta.subList(1, respuesta.size()));
        }
        if (Protocolo.ERROR.equals(tipo)) {
            throw new ErrorDelServidorException(respuesta.size() > 1 ? respuesta.get(1) : "error sin motivo");
        }
        throw new IOException("respuesta inesperada del servidor: " + tipo);
    }

    private static void cerrarSocket(Socket s) {
        if (s == null) {
            return;
        }
        try {
            s.close();
        } catch (IOException ignorada) {
            // Ya estaba cerrado: no hay nada más que hacer.
        }
    }

    private static Thread hiloDemonio(Runnable tarea, String nombre) {
        Thread hilo = new Thread(tarea, nombre);
        hilo.setDaemon(true);
        return hilo;
    }
}
