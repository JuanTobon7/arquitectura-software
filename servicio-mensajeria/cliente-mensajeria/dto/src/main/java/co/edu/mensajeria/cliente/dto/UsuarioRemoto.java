package co.edu.mensajeria.cliente.dto;

/**
 * Un usuario registrado en el servidor, tal como lo entrega {@code USUARIOS}.
 * El contacto especial {@link #todos()} representa la conversación con todos ({@code destino = *}).
 */
public record UsuarioRemoto(String codigo, String nombres, String apellidos, String programa, boolean conectado) {

    /** Destino del protocolo para enviar a todos los conectados. */
    public static final String TODOS = "*";

    /** Contacto de la conversación grupal. Siempre aparece como conectado. */
    public static UsuarioRemoto todos() {
        return new UsuarioRemoto(TODOS, "Todos", "", "", true);
    }

    public String nombreCompleto() {
        return (nombres + " " + apellidos).trim();
    }

    /** Copia con otro estado de conexión (llega una notificación de presencia). */
    public UsuarioRemoto conConexion(boolean estado) {
        return new UsuarioRemoto(codigo, nombres, apellidos, programa, estado);
    }
}
