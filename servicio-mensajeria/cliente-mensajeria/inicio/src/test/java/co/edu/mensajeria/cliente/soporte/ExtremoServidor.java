package co.edu.mensajeria.cliente.soporte;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

/**
 * El lado del servidor de una sola conexión, manejado a mano por la prueba: lee lo que
 * envía el cliente y responde lo que la prueba quiera, en el momento que quiera.
 */
public final class ExtremoServidor implements AutoCloseable {

    private final ServerSocket servidor;
    private Socket socket;
    private BufferedReader entrada;
    private PrintWriter salida;

    public ExtremoServidor() throws IOException {
        servidor = new ServerSocket(0);
        servidor.setSoTimeout(5000);
    }

    public int puerto() {
        return servidor.getLocalPort();
    }

    /** Espera a que el cliente se conecte. */
    public ExtremoServidor aceptar() throws IOException {
        socket = servidor.accept();
        socket.setSoTimeout(5000);
        entrada = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        salida = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
        return this;
    }

    /** La siguiente línea que envió el cliente, o {@code null} si cerró. */
    public String leer() throws IOException {
        try {
            return entrada.readLine();
        } catch (SocketTimeoutException e) {
            throw new AssertionError("el cliente no envió nada en 5 segundos", e);
        }
    }

    public void enviar(String linea) {
        salida.print(linea + "\n");
        salida.flush();
    }

    /** Corta la conexión como si el servidor se cayera. */
    public void cortar() throws IOException {
        socket.close();
    }

    @Override
    public void close() throws IOException {
        if (socket != null) {
            socket.close();
        }
        servidor.close();
    }
}
