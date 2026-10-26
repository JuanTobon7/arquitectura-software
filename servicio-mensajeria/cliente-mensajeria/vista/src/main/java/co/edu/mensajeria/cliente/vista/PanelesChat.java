package co.edu.mensajeria.cliente.vista;

import co.edu.mensajeria.cliente.dto.MensajeChat;
import co.edu.mensajeria.cliente.dto.UsuarioRemoto;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.function.Predicate;

/**
 * Arma los paneles de {@link VentanaChat} y da acceso a sus controles. Separa el "cómo se ve"
 * del "qué hace", que queda en la ventana.
 */
final class PanelesChat {

    static final Color BARRA = new Color(0x00, 0x80, 0x69);
    private static final Color GRIS = new Color(0x66, 0x77, 0x81);

    private final JList<UsuarioRemoto> listaUsuarios;
    private final JList<MensajeChat> listaMensajes;
    private final JTextField texto = new JTextField();
    private final JButton enviar = new JButton("Enviar");
    private final JButton adjuntar = new JButton("Adjuntar…");
    private final JButton cerrarSesion = new JButton("Cerrar sesión");
    private final JLabel nombreContacto = new JLabel("Elija un contacto");
    private final JLabel detalleContacto = new JLabel(" ");
    private final JLabel estado = new JLabel(" ");

    PanelesChat(DefaultListModel<UsuarioRemoto> usuarios, DefaultListModel<MensajeChat> conversacion,
                Predicate<String> noLeido, RenderizadorMensaje renderizador) {
        listaUsuarios = new JList<>(usuarios);
        listaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaUsuarios.setCellRenderer(new RenderizadorContacto(noLeido));
        listaMensajes = new JList<>(conversacion) {
            /** Siempre del ancho visible: así los mensajes propios quedan pegados a la derecha. */
            @Override
            public boolean getScrollableTracksViewportWidth() {
                return true;
            }
        };
        listaMensajes.setCellRenderer(renderizador);
        listaMensajes.setBackground(RenderizadorMensaje.FONDO);
        listaMensajes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        habilitarEnvio(false);
    }

    JPanel raiz(UsuarioRemoto yo) {
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.add(barraSuperior(yo), BorderLayout.NORTH);
        raiz.add(contactos(), BorderLayout.WEST);
        raiz.add(conversacion(), BorderLayout.CENTER);
        estado.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        estado.setForeground(GRIS);
        raiz.add(estado, BorderLayout.SOUTH);
        return raiz;
    }

    private JPanel barraSuperior(UsuarioRemoto yo) {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(BARRA);
        barra.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 10));
        JLabel titulo = new JLabel("<html><b>" + yo.nombreCompleto() + "</b>  <font color='#d0f0e8'>"
                + yo.codigo() + (yo.programa().isBlank() ? "" : " · " + yo.programa()) + "</font></html>");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(titulo.getFont().deriveFont(14f));
        barra.add(titulo, BorderLayout.CENTER);
        barra.add(cerrarSesion, BorderLayout.EAST);
        return barra;
    }

    private JScrollPane contactos() {
        JScrollPane panel = new JScrollPane(listaUsuarios);
        panel.setPreferredSize(new Dimension(270, 0));
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(0xD1, 0xD7, 0xDB)));
        return panel;
    }

    private JPanel conversacion() {
        JPanel cabecera = new JPanel(new GridLayout(2, 1));
        cabecera.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        nombreContacto.setFont(nombreContacto.getFont().deriveFont(Font.BOLD, 15f));
        detalleContacto.setForeground(GRIS);
        cabecera.add(nombreContacto);
        cabecera.add(detalleContacto);

        JScrollPane mensajes = new JScrollPane(listaMensajes,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        mensajes.setBorder(BorderFactory.createEmptyBorder());
        mensajes.getVerticalScrollBar().setUnitIncrement(16);

        JPanel escribir = new JPanel(new BorderLayout(8, 0));
        escribir.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        escribir.add(adjuntar, BorderLayout.WEST);
        escribir.add(texto, BorderLayout.CENTER);
        escribir.add(enviar, BorderLayout.EAST);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(cabecera, BorderLayout.NORTH);
        panel.add(mensajes, BorderLayout.CENTER);
        panel.add(escribir, BorderLayout.SOUTH);
        return panel;
    }

    void cabecera(UsuarioRemoto contacto) {
        nombreContacto.setText(contacto.nombreCompleto());
        if (UsuarioRemoto.TODOS.equals(contacto.codigo())) {
            detalleContacto.setText("Lo que escriba aquí les llega a todos los usuarios conectados");
        } else {
            detalleContacto.setText((contacto.conectado() ? "Conectado" : "Desconectado") + " · "
                    + contacto.codigo() + (contacto.programa().isBlank() ? "" : " · " + contacto.programa()));
        }
        habilitarEnvio(true);
    }

    void alEnviar(Runnable accion) {
        enviar.addActionListener(e -> accion.run());
        texto.addActionListener(e -> accion.run());
    }

    void alAdjuntar(Runnable accion) {
        adjuntar.addActionListener(e -> accion.run());
    }

    void alCerrarSesion(Runnable accion) {
        cerrarSesion.addActionListener(e -> accion.run());
    }

    void habilitarEnvio(boolean habilitado) {
        texto.setEnabled(habilitado);
        enviar.setEnabled(habilitado);
        adjuntar.setEnabled(habilitado);
    }

    void estado(String mensaje) {
        estado.setText(mensaje);
    }

    String textoEscrito() {
        return texto.getText();
    }

    void limpiarTexto() {
        texto.setText("");
    }

    void enfocarTexto() {
        texto.requestFocusInWindow();
    }

    /** Muestra el último mensaje, después de que la lista recalcula el alto de las filas. */
    void irAlFinal() {
        SwingUtilities.invokeLater(() -> {
            int ultimo = listaMensajes.getModel().getSize() - 1;
            if (ultimo >= 0) {
                listaMensajes.ensureIndexIsVisible(ultimo);
            }
        });
    }

    JList<UsuarioRemoto> listaUsuarios() {
        return listaUsuarios;
    }

    JList<MensajeChat> listaMensajes() {
        return listaMensajes;
    }
}
