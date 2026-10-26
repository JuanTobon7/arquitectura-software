package co.edu.mensajeria.cliente.dto;

import java.time.LocalDateTime;

/**
 * Un mensaje de una conversación, enviado o recibido, tal como se guarda en el historial local.
 *
 * @param id          id que asignó el servidor
 * @param propietario usuario de este cliente dueño del historial
 * @param contacto    con quién es la conversación: el otro usuario o {@code *} para todos
 * @param origen      código del remitente ({@code SERVIDOR} si lo envía el administrador)
 * @param destino     código del destinatario o {@code *}
 * @param contenido   el texto, o el nombre del archivo si es imagen o archivo
 * @param rutaArchivo dónde quedó guardado el archivo en este equipo; {@code null} si es texto
 */
public record MensajeChat(String id, String propietario, String contacto, String origen, String destino,
                          TipoMensaje tipo, LocalDateTime fecha, String contenido, String rutaArchivo) {

    public boolean enviadoPorMi() {
        return propietario.equals(origen);
    }
}
