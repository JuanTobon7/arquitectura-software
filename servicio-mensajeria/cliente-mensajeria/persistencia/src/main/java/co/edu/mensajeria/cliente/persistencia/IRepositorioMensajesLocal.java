package co.edu.mensajeria.cliente.persistencia;

import co.edu.mensajeria.cliente.dto.MensajeChat;

import java.util.List;

/**
 * Historial de conversaciones guardado en el equipo del usuario. El servidor no guarda
 * conversaciones para los clientes, así que sin él al cerrar el cliente se perdería todo.
 */
public interface IRepositorioMensajesLocal {

    /** Guarda el mensaje. Guardar dos veces el mismo id del mismo propietario no lo duplica. */
    void guardar(MensajeChat m);

    /** Mensajes entre {@code propietario} y {@code contacto}, del más antiguo al más reciente. */
    List<MensajeChat> conversacion(String propietario, String contacto);
}
