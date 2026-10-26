package co.edu.mensajeria.cliente.fachada;

import co.edu.mensajeria.cliente.dto.MensajeChat;

/**
 * Recibe lo que el servidor envía por su cuenta (patrón Observer). Los métodos se llaman
 * desde el hilo de notificaciones, no desde el de Swing: una vista debe pasar el trabajo
 * a su hilo (por ejemplo con {@code SwingUtilities.invokeLater}).
 */
public interface ObservadorCliente {

    /** Llegó un mensaje. Ya está guardado en el historial y, si trae archivo, en las descargas. */
    void mensajeRecibido(MensajeChat m);

    /** Un usuario entró (primera sesión) o salió (última sesión). */
    void presenciaCambiada(String codigo, boolean conectado);

    /** El servidor cerró la sesión o se perdió la conexión. Se avisa una sola vez. */
    void conexionCerrada(String motivo);
}
