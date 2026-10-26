package co.edu.mensajeria.cliente.comunicacion;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

/**
 * Canal con el servidor. Separa lo que llega en dos flujos: las respuestas a cada
 * petición ({@code OK} o {@code ERROR}) y las notificaciones asíncronas ({@code NOTIF}).
 */
public interface IConexionServidor {

    /** Tipos de notificación: el primer campo que recibe la escucha. */
    String NOTIF_MENSAJE = "MENSAJE";
    String NOTIF_PRESENCIA = "PRESENCIA";
    String NOTIF_CIERRE = "CIERRE";

    void conectar(String host, int puerto) throws IOException;

    /**
     * Envía una petición y espera su respuesta. Las peticiones se atienden de a una.
     *
     * @return los campos de la respuesta después de {@code OK}
     * @throws ErrorDelServidorException si el servidor respondió {@code ERROR|motivo}
     * @throws IOException               si no hay conexión, se perdió o el servidor no respondió a tiempo
     */
    List<String> pedir(String... campos) throws IOException;

    /**
     * Registra quién recibe las notificaciones, sin el {@code NOTIF} inicial: por ejemplo
     * {@code [PRESENCIA, 1002, 1]}. Se llama en un hilo propio, en el orden de llegada.
     * Si el servidor corta la conexión sin que se haya llamado a {@link #cerrar()}, la escucha
     * recibe {@code [CIERRE, motivo]}.
     */
    void alRecibirNotificacion(Consumer<List<String>> escucha);

    boolean conectada();

    /** Cierra la conexión. No genera notificación de cierre. Se puede llamar varias veces. */
    void cerrar();
}
