package co.edu.mensajeria.cliente.fachada;

import co.edu.mensajeria.cliente.dto.MensajeChat;
import co.edu.mensajeria.cliente.dto.UsuarioRemoto;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Lo único que la vista conoce del cliente (patrón Fachada). Los métodos que hablan con el
 * servidor bloquean hasta tener la respuesta: la vista los llama fuera del hilo de Swing.
 * Si el servidor responde {@code ERROR}, lanzan una {@link IOException} cuyo mensaje es el motivo.
 */
public interface IFachadaCliente {

    /** Se conecta y envía {@code LOGIN}. Si falla, deja la conexión cerrada. */
    UsuarioRemoto iniciarSesion(String host, int puerto, String codigo, String clave) throws IOException;

    UsuarioRemoto usuarioActual();

    /** Todos los usuarios registrados, con su estado de conexión. */
    List<UsuarioRemoto> listarUsuarios() throws IOException;

    /** @param destino código de un usuario o {@link UsuarioRemoto#TODOS} */
    MensajeChat enviarTexto(String destino, String texto) throws IOException;

    /** Envía un archivo; si es una imagen, el servidor le aplica todos sus filtros. */
    default MensajeChat enviarArchivo(String destino, Path archivo) throws IOException {
        return enviarArchivo(destino, archivo, List.of());
    }

    /**
     * Envía un archivo eligiendo los filtros que el servidor aplica si es una imagen, en ese orden.
     * Con la lista vacía el servidor aplica todos los suyos.
     */
    MensajeChat enviarArchivo(String destino, Path archivo, List<String> filtros) throws IOException;

    /** Historial local con un contacto, del más antiguo al más reciente. */
    List<MensajeChat> conversacion(String contacto);

    /** Filtros de imagen que se pueden elegir al enviar. */
    List<String> filtrosDisponibles();

    void agregarObservador(ObservadorCliente o);

    /** Envía {@code SALIR} y cierra la conexión. Los observadores no reciben aviso de cierre. */
    void cerrarSesion();
}
