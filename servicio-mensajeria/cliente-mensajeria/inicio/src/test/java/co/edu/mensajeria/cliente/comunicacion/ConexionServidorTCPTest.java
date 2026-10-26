package co.edu.mensajeria.cliente.comunicacion;

import co.edu.mensajeria.cliente.soporte.ExtremoServidor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static co.edu.mensajeria.cliente.soporte.RegistroObservador.esperar;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ConexionServidorTCP: respuestas, notificaciones y cierres")
class ConexionServidorTCPTest {

    private final BlockingQueue<List<String>> notificaciones = new LinkedBlockingQueue<>();
    private ConexionServidorTCP conexion;
    private ExtremoServidor servidor;

    @BeforeEach
    void conectar() throws IOException {
        servidor = new ExtremoServidor();
        conexion = new ConexionServidorTCP(Duration.ofMillis(800));
        conexion.alRecibirNotificacion(notificaciones::add);
        conexion.conectar("localhost", servidor.puerto());
        servidor.aceptar();
    }

    @AfterEach
    void cerrar() throws IOException {
        conexion.cerrar();
        servidor.close();
    }

    /** Envía una petición en otro hilo; la prueba hace de servidor. */
    private CompletableFuture<List<String>> pedirEnOtroHilo(String... campos) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return conexion.pedir(campos);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    @DisplayName("pedir envía la línea escapada y devuelve los campos después de OK")
    void pedirDevuelveLosCampos() throws Exception {
        CompletableFuture<List<String>> respuesta = pedirEnOtroHilo("TEXTO", "1002", "hola|mundo");
        assertThat(servidor.leer()).isEqualTo("TEXTO|1002|hola\\|mundo");
        servidor.enviar("OK|id-1");
        assertThat(respuesta.get(5, TimeUnit.SECONDS)).containsExactly("id-1");
    }

    @Test
    @DisplayName("ERROR|motivo se convierte en ErrorDelServidorException con el motivo")
    void errorDelServidor() throws Exception {
        CompletableFuture<List<String>> respuesta = pedirEnOtroHilo("LOGIN", "1001", "mala");
        servidor.leer();
        servidor.enviar("ERROR|credenciales inválidas");
        assertThatThrownBy(() -> respuesta.get(5, TimeUnit.SECONDS))
                .hasCauseInstanceOf(RuntimeException.class)
                .rootCause().isInstanceOf(ErrorDelServidorException.class).hasMessage("credenciales inválidas");
    }

    @Test
    @DisplayName("Una notificación que llega antes de la respuesta va a la escucha, sin el NOTIF")
    void notificacionIntercalada() throws Exception {
        CompletableFuture<List<String>> respuesta = pedirEnOtroHilo("USUARIOS");
        servidor.leer();
        servidor.enviar("NOTIF|PRESENCIA|1003|1");
        servidor.enviar("OK|0");
        assertThat(respuesta.get(5, TimeUnit.SECONDS)).containsExactly("0");
        assertThat(esperar(notificaciones)).containsExactly("PRESENCIA", "1003", "1");
    }

    @Test
    @DisplayName("Las notificaciones llegan en el orden en que las envió el servidor")
    void ordenDeNotificaciones() throws Exception {
        for (int i = 0; i < 20; i++) {
            servidor.enviar("NOTIF|MENSAJE|" + i);
        }
        for (int i = 0; i < 20; i++) {
            assertThat(esperar(notificaciones)).containsExactly("MENSAJE", String.valueOf(i));
        }
    }

    @Test
    @DisplayName("Si el servidor corta, la escucha recibe CIERRE y la petición pendiente falla enseguida")
    void servidorCorta() throws Exception {
        CompletableFuture<List<String>> respuesta = pedirEnOtroHilo("USUARIOS");
        servidor.leer();
        servidor.cortar();
        assertThat(esperar(notificaciones)).containsExactly("CIERRE", ConexionServidorTCP.MOTIVO_CONEXION_PERDIDA);
        assertThatThrownBy(() -> respuesta.get(2, TimeUnit.SECONDS))
                .rootCause().isInstanceOf(IOException.class).hasMessageContaining("se perdió la conexión");
        assertThat(conexion.conectada()).isFalse();
    }

    @Test
    @DisplayName("cerrar() no genera notificación de cierre y después pedir falla")
    void cerrarSinAviso() throws Exception {
        conexion.cerrar();
        assertThat(servidor.leer()).isNull();
        assertThat(notificaciones.poll(300, TimeUnit.MILLISECONDS)).isNull();
        assertThat(conexion.conectada()).isFalse();
        assertThatThrownBy(() -> conexion.pedir("USUARIOS")).isInstanceOf(IOException.class)
                .hasMessage("no hay conexión con el servidor");
        conexion.cerrar(); // se puede llamar otra vez
    }

    @Test
    @DisplayName("Si el servidor no responde a tiempo, pedir falla y la conexión se cierra")
    void sinRespuesta() throws Exception {
        assertThatThrownBy(() -> conexion.pedir("USUARIOS")).isInstanceOf(IOException.class)
                .hasMessageContaining("no respondió");
        assertThat(conexion.conectada()).isFalse();
    }

    @Test
    @DisplayName("Una respuesta que no es OK ni ERROR es un error de protocolo")
    void respuestaInesperada() throws Exception {
        CompletableFuture<List<String>> respuesta = pedirEnOtroHilo("USUARIOS");
        servidor.leer();
        servidor.enviar("HOLA|x");
        assertThatThrownBy(() -> respuesta.get(5, TimeUnit.SECONDS))
                .rootCause().hasMessageContaining("respuesta inesperada");
    }

    @Test
    @DisplayName("Con varios hilos, cada petición recibe su propia respuesta")
    void peticionesDeAUna() throws Exception {
        CompletableFuture<List<String>> primera = pedirEnOtroHilo("TEXTO", "1002", "a");
        String linea1 = servidor.leer();
        CompletableFuture<List<String>> segunda = pedirEnOtroHilo("TEXTO", "1002", "b");
        Thread.sleep(150);
        servidor.enviar("OK|" + linea1.substring(linea1.length() - 1));
        String linea2 = servidor.leer();
        servidor.enviar("OK|" + linea2.substring(linea2.length() - 1));
        assertThat(primera.get(5, TimeUnit.SECONDS)).containsExactly("a");
        assertThat(segunda.get(5, TimeUnit.SECONDS)).containsExactly("b");
    }

    @Test
    @DisplayName("Después de cerrar se puede conectar otra vez")
    void reconectar() throws Exception {
        conexion.cerrar();
        try (ExtremoServidor otro = new ExtremoServidor()) {
            conexion.conectar("localhost", otro.puerto());
            otro.aceptar();
            CompletableFuture<List<String>> respuesta = pedirEnOtroHilo("SALIR");
            assertThat(otro.leer()).isEqualTo("SALIR");
            otro.enviar("OK");
            assertThat(respuesta.get(5, TimeUnit.SECONDS)).isEmpty();
        }
    }

    @Test
    @DisplayName("Conectar a un puerto sin servidor falla con un mensaje que dice a dónde")
    void puertoCerrado() throws Exception {
        int puertoLibre;
        try (ServerSocket s = new ServerSocket(0)) {
            puertoLibre = s.getLocalPort();
        }
        ConexionServidorTCP otra = new ConexionServidorTCP();
        assertThatThrownBy(() -> otra.conectar("localhost", puertoLibre)).isInstanceOf(IOException.class)
                .hasMessageContaining("no se pudo conectar con localhost:" + puertoLibre);
        assertThat(otra.conectada()).isFalse();
    }
}
