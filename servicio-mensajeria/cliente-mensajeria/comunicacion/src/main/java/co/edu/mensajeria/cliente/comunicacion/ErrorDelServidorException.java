package co.edu.mensajeria.cliente.comunicacion;

import java.io.IOException;

/** El servidor respondió {@code ERROR|motivo}. El mensaje de la excepción es el motivo. */
public class ErrorDelServidorException extends IOException {

    public ErrorDelServidorException(String motivo) {
        super(motivo);
    }
}
