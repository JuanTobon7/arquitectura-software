package co.edu.mensajeria.cliente.soporte;

import co.edu.mensajeria.cliente.dto.MensajeChat;
import co.edu.mensajeria.cliente.fachada.ObservadorCliente;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/** Observador que guarda lo que recibe, para que la prueba lo espere y lo revise. */
public final class RegistroObservador implements ObservadorCliente {

    public final BlockingQueue<MensajeChat> mensajes = new LinkedBlockingQueue<>();
    public final BlockingQueue<String> presencias = new LinkedBlockingQueue<>();
    public final BlockingQueue<String> cierres = new LinkedBlockingQueue<>();

    @Override
    public void mensajeRecibido(MensajeChat m) {
        mensajes.add(m);
    }

    @Override
    public void presenciaCambiada(String codigo, boolean conectado) {
        presencias.add(codigo + "=" + conectado);
    }

    @Override
    public void conexionCerrada(String motivo) {
        cierres.add(motivo);
    }

    /** Espera hasta 5 segundos el siguiente elemento; falla si no llega. */
    public static <T> T esperar(BlockingQueue<T> cola) throws InterruptedException {
        T valor = cola.poll(5, TimeUnit.SECONDS);
        if (valor == null) {
            throw new AssertionError("no llegó nada en 5 segundos");
        }
        return valor;
    }
}
