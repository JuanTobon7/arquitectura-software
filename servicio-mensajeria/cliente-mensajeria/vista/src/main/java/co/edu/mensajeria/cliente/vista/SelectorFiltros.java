package co.edu.mensajeria.cliente.vista;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Diálogo para elegir los filtros de una imagen. Se aplican en el orden en que se marcan;
 * si no se marca ninguno, el servidor aplica todos los suyos.
 */
final class SelectorFiltros {

    static final String SIN_ELEGIR = "Ninguno elegido: el servidor aplica todos sus filtros";
    /** Los filtros que trae el servidor. Los plugins nuevos se muestran con su nombre técnico. */
    private static final Map<String, String> NOMBRES = Map.of(
            "escala_grises", "Escala de grises", "sepia", "Sepia", "girar_180", "Girar 180°",
            "brillo", "Brillo", "reduccion", "Reducción");

    private final List<String> elegidos = new ArrayList<>();
    private final JLabel orden = new JLabel(SIN_ELEGIR);
    private final JPanel panel = new JPanel();

    SelectorFiltros(List<String> disponibles) {
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel("Marque los filtros en el orden en que quiere aplicarlos:"));
        panel.add(Box.createVerticalStrut(6));
        for (String filtro : disponibles) {
            JCheckBox casilla = new JCheckBox(legible(filtro));
            casilla.addActionListener(e -> marcar(filtro, casilla.isSelected()));
            panel.add(casilla);
        }
        orden.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        panel.add(orden);
    }

    /** Muestra el diálogo. Vacío si el usuario cancela. */
    static Optional<List<String>> elegir(Component padre, String archivo, List<String> disponibles) {
        SelectorFiltros selector = new SelectorFiltros(disponibles);
        int opcion = JOptionPane.showConfirmDialog(padre, selector.panel, "Filtros para " + archivo,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        return opcion == JOptionPane.OK_OPTION ? Optional.of(selector.elegidos()) : Optional.empty();
    }

    void marcar(String filtro, boolean marcado) {
        elegidos.remove(filtro);
        if (marcado) {
            elegidos.add(filtro);
        }
        orden.setText(elegidos.isEmpty() ? SIN_ELEGIR
                : "Orden: " + String.join(" → ", elegidos.stream().map(SelectorFiltros::legible).toList()));
    }

    List<String> elegidos() {
        return List.copyOf(elegidos);
    }

    String textoOrden() {
        return orden.getText();
    }

    /** Nombre para mostrar: {@code girar_180} → {@code Girar 180°}; un filtro nuevo, {@code mi_filtro} → {@code Mi filtro}. */
    static String legible(String filtro) {
        String conocido = NOMBRES.get(filtro);
        if (conocido != null) {
            return conocido;
        }
        String texto = filtro.replace('_', ' ');
        return texto.isEmpty() ? texto : Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }
}
