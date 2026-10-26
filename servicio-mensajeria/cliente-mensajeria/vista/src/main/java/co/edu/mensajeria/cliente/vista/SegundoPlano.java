package co.edu.mensajeria.cliente.vista;

import javax.swing.SwingUtilities;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Ejecuta fuera del hilo de Swing lo que habla con el servidor, para que la ventana no se
 * congele, y vuelve al hilo de Swing con el resultado. Usa un solo hilo: los mensajes salen
 * en el mismo orden en que el usuario los envió.
 */
final class SegundoPlano {

    private final ExecutorService hilo;

    SegundoPlano(String nombre) {
        hilo = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, nombre);
            t.setDaemon(true);
            return t;
        });
    }

    /** {@code alTerminar} y {@code alFallar} se llaman en el hilo de Swing. */
    <T> void ejecutar(Callable<T> trabajo, Consumer<T> alTerminar, Consumer<String> alFallar) {
        hilo.execute(() -> {
            try {
                T resultado = trabajo.call();
                SwingUtilities.invokeLater(() -> alTerminar.accept(resultado));
            } catch (Exception e) {
                String motivo = motivo(e);
                SwingUtilities.invokeLater(() -> alFallar.accept(motivo));
            }
        });
    }

    void detener() {
        hilo.shutdown();
    }

    /** El motivo que se le muestra al usuario, con mayúscula inicial. */
    static String motivo(Throwable e) {
        String texto = e.getMessage();
        if (texto == null || texto.isBlank()) {
            texto = e.getClass().getSimpleName();
        }
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }
}
