package co.edu.mensajeria.cliente.persistencia;

/** Falla de la base de datos local. No es recuperable por quien llama, por eso no es comprobada. */
public class ErrorPersistencia extends RuntimeException {

    public ErrorPersistencia(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
