package co.edu.demo.observabilidad;

import org.hibernate.resource.jdbc.spi.StatementInspector;

/**
 * Cuenta las sentencias SQL que ejecuta Hibernate en el hilo de cada request.
 * Además simula ~2 ms de ida y vuelta a la base de datos (H2 en memoria es
 * demasiado rápida para que el N+1 se note como en producción).
 */
public class ContadorSql implements StatementInspector {

    private static final ThreadLocal<Integer> CONSULTAS = ThreadLocal.withInitial(() -> 0);
    private static final long LATENCIA_RED_BD_MS = 2;

    @Override
    public String inspect(String sql) {
        CONSULTAS.set(CONSULTAS.get() + 1);
        try {
            Thread.sleep(LATENCIA_RED_BD_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return sql;
    }

    public static int consultasYReiniciar() {
        int total = CONSULTAS.get();
        CONSULTAS.remove();
        return total;
    }
}
