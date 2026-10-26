package co.edu.mensajeria.cliente.vista;

import co.edu.mensajeria.cliente.dto.UsuarioRemoto;
import co.edu.mensajeria.cliente.fachada.IFachadaCliente;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

/**
 * Inicio de sesión: servidor, puerto, código y clave. Es modal: {@link #mostrar()} vuelve
 * cuando el usuario entró o cerró la ventana.
 */
public class VentanaLogin extends JDialog {

    private static final Color ERROR = new Color(0xC6, 0x28, 0x28);

    private final IFachadaCliente fachada;
    private final SegundoPlano segundoPlano = new SegundoPlano("inicio-sesion");
    private final JTextField servidor;
    private final JTextField puerto;
    private final JTextField codigo = new JTextField(14);
    private final JPasswordField clave = new JPasswordField(14);
    private final JButton ingresar = new JButton("Ingresar");
    private final JLabel estado = new JLabel(" ");
    private boolean autenticado;

    public VentanaLogin(IFachadaCliente fachada, String host, int puertoPorDefecto) {
        super((Frame) null, "Mensajería académica - Iniciar sesión", true);
        this.fachada = fachada;
        this.servidor = new JTextField(host, 14);
        this.puerto = new JTextField(String.valueOf(puertoPorDefecto), 6);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setContentPane(formulario());
        getRootPane().setDefaultButton(ingresar);
        ingresar.addActionListener(e -> ingresar());
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                codigo.requestFocusInWindow();
            }
        });
    }

    /** Muestra la ventana y espera. Devuelve {@code true} si el usuario inició sesión. */
    public boolean mostrar() {
        setVisible(true);
        segundoPlano.detener();
        return autenticado;
    }

    private JPanel formulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(18, 22, 14, 22));
        JLabel titulo = new JLabel("Mensajería académica");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));
        agregar(panel, titulo, 0, 0, 2);
        agregar(panel, new JLabel("Ingrese con su código y su clave."), 0, 1, 2);
        List<JComponent> campos = List.of(servidor, puerto, codigo, clave);
        List<String> nombres = List.of("Servidor", "Puerto", "Código", "Clave");
        for (int i = 0; i < campos.size(); i++) {
            agregar(panel, new JLabel(nombres.get(i) + ":"), 0, i + 2, 1);
            agregar(panel, campos.get(i), 1, i + 2, 1);
        }
        estado.setForeground(ERROR);
        agregar(panel, estado, 0, 6, 2);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        botones.add(ingresar);
        agregar(panel, botones, 0, 7, 2);
        return panel;
    }

    private static void agregar(JPanel panel, JComponent c, int columna, int fila, int ancho) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = columna;
        g.gridy = fila;
        g.gridwidth = ancho;
        g.anchor = GridBagConstraints.WEST;
        g.fill = columna == 1 ? GridBagConstraints.HORIZONTAL : GridBagConstraints.NONE;
        g.insets = new Insets(4, 4, 4, 4);
        panel.add(c, g);
    }

    private void ingresar() {
        String host = servidor.getText().strip();
        String usuario = codigo.getText().strip();
        String contrasena = new String(clave.getPassword());
        int numeroPuerto;
        try {
            numeroPuerto = Integer.parseInt(puerto.getText().strip());
        } catch (NumberFormatException e) {
            mostrarError("El puerto debe ser un número");
            return;
        }
        if (host.isEmpty() || usuario.isEmpty() || contrasena.isEmpty()) {
            mostrarError("Escriba el servidor, el código y la clave");
            return;
        }
        habilitar(false);
        estado.setForeground(Color.DARK_GRAY);
        estado.setText("Conectando con " + host + ":" + numeroPuerto + "…");
        segundoPlano.ejecutar(() -> fachada.iniciarSesion(host, numeroPuerto, usuario, contrasena),
                this::entrar, this::mostrarError);
    }

    private void entrar(UsuarioRemoto usuario) {
        autenticado = true;
        dispose();
    }

    private void mostrarError(String motivo) {
        habilitar(true);
        estado.setForeground(ERROR);
        estado.setText(motivo);
        pack();
        clave.requestFocusInWindow();
        clave.selectAll();
    }

    private void habilitar(boolean habilitado) {
        for (JComponent c : List.of(servidor, puerto, codigo, clave, ingresar)) {
            c.setEnabled(habilitado);
        }
    }
}
