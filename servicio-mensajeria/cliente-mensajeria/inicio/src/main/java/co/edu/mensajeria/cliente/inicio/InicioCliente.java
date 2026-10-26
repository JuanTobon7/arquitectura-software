package co.edu.mensajeria.cliente.inicio;

import co.edu.mensajeria.cliente.dto.ConfiguracionCliente;
import co.edu.mensajeria.cliente.fachada.FabricaCliente;
import co.edu.mensajeria.cliente.fachada.IFachadaCliente;
import co.edu.mensajeria.cliente.utilerias.LectorConfiguracion;
import co.edu.mensajeria.cliente.vista.VentanaChat;
import co.edu.mensajeria.cliente.vista.VentanaLogin;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Arranque del cliente: lee la configuración, arma la fachada con {@link FabricaCliente} y
 * abre el inicio de sesión. Al cerrar sesión se vuelve a empezar con una fachada nueva.
 *
 * <pre>java -jar cliente-mensajeria.jar [config/cliente.properties]</pre>
 */
public final class InicioCliente {

    static final String CONFIGURACION_POR_DEFECTO = "config/cliente.properties";

    private InicioCliente() {
    }

    public static void main(String[] args) {
        Locale.setDefault(Locale.forLanguageTag("es")); // botones de Swing en español: Aceptar, Cancelar…
        Path archivo = Path.of(args.length > 0 ? args[0] : CONFIGURACION_POR_DEFECTO);
        ConfiguracionCliente configuracion;
        try {
            configuracion = LectorConfiguracion.leer(archivo);
        } catch (IOException | IllegalArgumentException e) {
            fallar("No se pudo leer la configuración " + archivo + ":\n" + e.getMessage());
            return;
        }
        SwingUtilities.invokeLater(() -> {
            aplicarApariencia();
            iniciarSesion(configuracion);
        });
    }

    /** Muestra el inicio de sesión y, si el usuario entra, la ventana de chat. */
    static void iniciarSesion(ConfiguracionCliente configuracion) {
        IFachadaCliente fachada;
        try {
            fachada = FabricaCliente.crear(configuracion);
        } catch (RuntimeException e) {
            fallar(e.getMessage());
            return;
        }
        VentanaLogin login = new VentanaLogin(fachada, configuracion.host(), configuracion.puerto());
        if (!login.mostrar()) {
            System.exit(0);
        }
        new VentanaChat(fachada, () -> iniciarSesion(configuracion)).setVisible(true);
    }

    /** El aspecto del sistema operativo (Windows, macOS…); si no se puede, el de Java. */
    private static void aplicarApariencia() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ReflectiveOperationException | UnsupportedLookAndFeelException e) {
            // Se queda el aspecto por defecto de Swing.
        }
    }

    private static void fallar(String mensaje) {
        System.err.println(mensaje);
        if (!GraphicsEnvironment.isHeadless()) {
            JOptionPane.showMessageDialog(null, mensaje, "Cliente de mensajería", JOptionPane.ERROR_MESSAGE);
        }
        System.exit(1);
    }
}
