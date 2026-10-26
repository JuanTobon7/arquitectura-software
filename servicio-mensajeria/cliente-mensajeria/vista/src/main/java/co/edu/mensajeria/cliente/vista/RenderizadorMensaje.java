package co.edu.mensajeria.cliente.vista;

import co.edu.mensajeria.cliente.dto.MensajeChat;
import co.edu.mensajeria.cliente.dto.TipoMensaje;
import co.edu.mensajeria.cliente.dto.UsuarioRemoto;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Dibuja cada mensaje como una burbuja: a la derecha y en verde los propios, a la izquierda
 * los recibidos. Las imágenes se muestran en miniatura; las miniaturas se guardan en memoria
 * para no leer el archivo cada vez que la lista se repinta.
 */
public class RenderizadorMensaje implements ListCellRenderer<MensajeChat> {

    static final Color FONDO = new Color(0xEF, 0xEA, 0xE2);
    static final Color PROPIO = new Color(0xD9, 0xFD, 0xD3);
    static final Color AJENO = Color.WHITE;
    static final Color DEL_SERVIDOR = new Color(0xFF, 0xF4, 0xC2);
    static final int ANCHO_TEXTO = 320;
    static final int LADO_MINIATURA = 220;
    static final String SERVIDOR = "SERVIDOR";
    private static final Color GRIS = new Color(0x66, 0x77, 0x81);
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FECHA_Y_HORA = DateTimeFormatter.ofPattern("dd/MM HH:mm");
    /** Se guarda cuando la imagen no se pudo leer, para no intentarlo en cada repintado. */
    private static final ImageIcon SIN_MINIATURA = new ImageIcon();

    private final Map<String, ImageIcon> miniaturas = new HashMap<>();
    private final Function<String, String> nombres;

    /** @param nombres nombre que se muestra para cada código de usuario (y para {@code SERVIDOR}) */
    public RenderizadorMensaje(Function<String, String> nombres) {
        this.nombres = nombres;
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends MensajeChat> lista, MensajeChat m, int indice,
                                                  boolean seleccionado, boolean conFoco) {
        boolean propio = m.enviadoPorMi();
        Burbuja burbuja = new Burbuja(colorDe(m));
        if (!propio && UsuarioRemoto.TODOS.equals(m.contacto())) {
            burbuja.add(etiqueta(nombres.apply(m.origen()), Font.BOLD, new Color(0x1F, 0x7A, 0x5C)));
        }
        burbuja.add(contenido(m));
        burbuja.add(etiqueta(hora(m), Font.PLAIN, GRIS));

        JPanel fila = new JPanel(new FlowLayout(propio ? FlowLayout.RIGHT : FlowLayout.LEFT, 10, 3));
        fila.setBackground(seleccionado ? FONDO.darker() : FONDO);
        fila.add(burbuja);
        if (m.rutaArchivo() != null) {
            fila.setToolTipText("Doble clic para abrir " + m.contenido());
        }
        return fila;
    }

    private Component contenido(MensajeChat m) {
        if (m.tipo() == TipoMensaje.TEXTO) {
            return textoAjustado(m.contenido());
        }
        JLabel contenido;
        ImageIcon miniatura = m.tipo() == TipoMensaje.IMAGEN ? miniatura(m.rutaArchivo()) : SIN_MINIATURA;
        if (miniatura != SIN_MINIATURA) {
            contenido = new JLabel(miniatura);
        } else {
            contenido = new JLabel(html(m.contenido()), UIManager.getIcon("FileView.fileIcon"), JLabel.LEFT);
            contenido.setFont(contenido.getFont().deriveFont(Font.BOLD));
        }
        contenido.setAlignmentX(Component.LEFT_ALIGNMENT);
        return contenido;
    }

    /** Miniatura de la imagen, leída una sola vez. */
    ImageIcon miniatura(String ruta) {
        if (ruta == null) {
            return SIN_MINIATURA;
        }
        return miniaturas.computeIfAbsent(ruta, RenderizadorMensaje::leerMiniatura);
    }

    private static ImageIcon leerMiniatura(String ruta) {
        try {
            BufferedImage original = ImageIO.read(new File(ruta));
            if (original == null) {
                return SIN_MINIATURA;
            }
            double escala = Math.min(1.0, (double) LADO_MINIATURA / Math.max(original.getWidth(), original.getHeight()));
            int ancho = Math.max(1, (int) Math.round(original.getWidth() * escala));
            int alto = Math.max(1, (int) Math.round(original.getHeight() * escala));
            return new ImageIcon(original.getScaledInstance(ancho, alto, Image.SCALE_SMOOTH));
        } catch (IOException | RuntimeException e) {
            return SIN_MINIATURA;
        }
    }

    private static Color colorDe(MensajeChat m) {
        if (SERVIDOR.equals(m.origen())) {
            return DEL_SERVIDOR;
        }
        return m.enviadoPorMi() ? PROPIO : AJENO;
    }

    private static String hora(MensajeChat m) {
        boolean deHoy = m.fecha().toLocalDate().equals(LocalDate.now());
        return m.fecha().format(deHoy ? HORA : FECHA_Y_HORA);
    }

    private static JLabel etiqueta(String texto, int estilo, Color color) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(etiqueta.getFont().deriveFont(estilo, etiqueta.getFont().getSize2D() - 1f));
        etiqueta.setForeground(color);
        etiqueta.setAlignmentX(Component.LEFT_ALIGNMENT);
        return etiqueta;
    }

    /**
     * Texto que hace saltos de línea a {@link #ANCHO_TEXTO} píxeles. Si cabe en una línea, la
     * burbuja queda del ancho del texto.
     */
    static JTextArea textoAjustado(String texto) {
        JTextArea area = new JTextArea(texto == null ? "" : texto);
        area.setFont(UIManager.getFont("Label.font"));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);
        area.setOpaque(false);
        area.setBorder(BorderFactory.createEmptyBorder());
        FontMetrics medidas = area.getFontMetrics(area.getFont());
        int natural = area.getText().lines().mapToInt(medidas::stringWidth).max().orElse(0) + 2;
        int ancho = Math.min(ANCHO_TEXTO, natural);
        area.setSize(ancho, Short.MAX_VALUE); // con el ancho fijo, el alto preferido es el del texto ajustado
        Dimension tamano = new Dimension(ancho, area.getPreferredSize().height);
        // BoxLayout respeta el mínimo y el máximo: se fijan para que no cambie el alto.
        area.setPreferredSize(tamano);
        area.setMinimumSize(tamano);
        area.setMaximumSize(tamano);
        area.setAlignmentX(Component.LEFT_ALIGNMENT);
        return area;
    }

    /** Texto en HTML: se escapan los caracteres especiales y los saltos de línea pasan a {@code <br>}. */
    static String html(String texto) {
        String seguro = texto == null ? "" : texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        return "<html>" + seguro.replace("\n", "<br>") + "</html>";
    }

    /** Panel con esquinas redondeadas que contiene un mensaje. */
    private static final class Burbuja extends JPanel {

        private final Color color;

        Burbuja(Color color) {
            this.color = color;
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0, 0, 0, 25));
            g2.fillRoundRect(0, 1, getWidth(), getHeight() - 1, 14, 14);
            g2.setColor(color);
            g2.fillRoundRect(0, 0, getWidth(), getHeight() - 1, 14, 14);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
