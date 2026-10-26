package co.edu.mensajeria.cliente.comunicacion;

import java.util.ArrayList;
import java.util.List;

/**
 * Formato de línea del servidor: texto UTF-8, una línea por mensaje y campos separados por '|'.
 *
 * <p>Escape dentro de un campo: {@code \|} es un '|' literal, {@code \\} es '\', {@code \n} y
 * {@code \r} son saltos de línea. Así un texto con cualquier carácter cabe en una sola línea.
 * Es el mismo algoritmo de {@code ProtocoloComunicacion} del servidor.</p>
 */
public final class Protocolo {

    public static final String OK = "OK";
    public static final String ERROR = "ERROR";
    public static final String NOTIFICACION = "NOTIF";

    static final char SEPARADOR = '|';
    static final char ESCAPE = '\\';

    private Protocolo() {
    }

    /** Une campos escapándolos. Un campo {@code null} se envía vacío. */
    public static String unir(String... campos) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < campos.length; i++) {
            if (i > 0) {
                sb.append(SEPARADOR);
            }
            escapar(campos[i], sb);
        }
        return sb.toString();
    }

    /** Separa una línea en campos deshaciendo el escape. Siempre devuelve al menos un campo. */
    public static List<String> separar(String linea) {
        List<String> campos = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);
            if (c == ESCAPE && i + 1 < linea.length()) {
                char siguiente = linea.charAt(++i);
                switch (siguiente) {
                    case 'n' -> actual.append('\n');
                    case 'r' -> actual.append('\r');
                    default -> actual.append(siguiente);
                }
            } else if (c == SEPARADOR) {
                campos.add(actual.toString());
                actual.setLength(0);
            } else {
                actual.append(c);
            }
        }
        campos.add(actual.toString());
        return campos;
    }

    private static void escapar(String valor, StringBuilder sb) {
        if (valor == null) {
            return;
        }
        for (int i = 0; i < valor.length(); i++) {
            char c = valor.charAt(i);
            switch (c) {
                case SEPARADOR -> sb.append(ESCAPE).append(SEPARADOR);
                case ESCAPE -> sb.append(ESCAPE).append(ESCAPE);
                case '\n' -> sb.append(ESCAPE).append('n');
                case '\r' -> sb.append(ESCAPE).append('r');
                default -> sb.append(c);
            }
        }
    }
}
