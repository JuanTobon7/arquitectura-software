package co.edu.mensajeria.cliente.vista;

import co.edu.mensajeria.cliente.dto.UsuarioRemoto;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.function.Predicate;

/** Cada contacto con un punto verde si está conectado y en negrita si tiene mensajes sin leer. */
class RenderizadorContacto extends DefaultListCellRenderer {

    static final Color CONECTADO = new Color(0x25, 0xD3, 0x66);
    static final Color DESCONECTADO = new Color(0xB0, 0xB8, 0xBE);
    private static final Color NO_LEIDO = new Color(0x1E, 0x88, 0xE5);

    private final Predicate<String> tieneNoLeidos;

    RenderizadorContacto(Predicate<String> tieneNoLeidos) {
        this.tieneNoLeidos = tieneNoLeidos;
    }

    @Override
    public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice, boolean seleccionado,
                                                  boolean conFoco) {
        JLabel etiqueta = (JLabel) super.getListCellRendererComponent(lista, valor, indice, seleccionado, false);
        UsuarioRemoto u = (UsuarioRemoto) valor;
        etiqueta.setText(texto(u, tieneNoLeidos.test(u.codigo())));
        etiqueta.setIcon(new Punto(u.conectado() ? CONECTADO : DESCONECTADO));
        etiqueta.setIconTextGap(10);
        etiqueta.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        return etiqueta;
    }

    static String texto(UsuarioRemoto u, boolean noLeido) {
        String nombre = noLeido ? "<b>" + u.nombreCompleto() + "</b>" : u.nombreCompleto();
        String aviso = noLeido ? " <font color='" + hex(NO_LEIDO) + "'>&#9679; nuevo</font>" : "";
        String detalle = UsuarioRemoto.TODOS.equals(u.codigo())
                ? "Mensaje para todos los conectados"
                : u.codigo() + (u.programa().isBlank() ? "" : " · " + u.programa());
        return "<html>" + nombre + aviso + "<br><font size='-2' color='#667781'>" + detalle + "</font></html>";
    }

    private static String hex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    /** Círculo de color que indica el estado de conexión. */
    record Punto(Color color) implements Icon {

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillOval(x, y, getIconWidth(), getIconHeight());
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 10;
        }

        @Override
        public int getIconHeight() {
            return 10;
        }
    }
}
