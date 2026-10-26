package co.edu.mensajeria.cliente.fachada;

import co.edu.mensajeria.cliente.comunicacion.ErrorDelServidorException;
import co.edu.mensajeria.cliente.comunicacion.IConexionServidor;
import co.edu.mensajeria.cliente.dto.MensajeChat;
import co.edu.mensajeria.cliente.dto.TipoMensaje;
import co.edu.mensajeria.cliente.dto.UsuarioRemoto;
import co.edu.mensajeria.cliente.persistencia.ErrorPersistencia;
import co.edu.mensajeria.cliente.persistencia.IRepositorioMensajesLocal;
import co.edu.mensajeria.cliente.soporte.RegistroObservador;
import co.edu.mensajeria.cliente.soporte.RepositorioEnMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("FachadaCliente: casos de uso")
class FachadaClienteTest {

    @TempDir
    Path descargas;

    private final IConexionServidor conexion = mock(IConexionServidor.class);
    private final RepositorioEnMemoria historial = new RepositorioEnMemoria();
    private final RegistroObservador observador = new RegistroObservador();
    private FachadaCliente fachada;

    @BeforeEach
    void crear() {
        fachada = new FachadaCliente(conexion, historial, descargas, List.of("sepia", "brillo"), 1000);
        fachada.agregarObservador(observador);
    }

    /** Inicia sesión como Ana (1001). */
    private void entrarComoAna() throws IOException {
        when(conexion.pedir("LOGIN", "1001", "clave1001"))
                .thenReturn(List.of("1001", "Ana María", "Gómez Ruiz", "Ingeniería de Sistemas"));
        fachada.iniciarSesion("localhost", 5000, "1001", "clave1001");
    }

    private static byte[] png() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(4, 3, BufferedImage.TYPE_INT_RGB), "png", bytes);
        return bytes.toByteArray();
    }

    @Nested
    @DisplayName("Sesión")
    class Sesion {

        @Test
        @DisplayName("iniciarSesion registra la escucha, se conecta, envía LOGIN y devuelve el perfil")
        void iniciarSesion() throws IOException {
            entrarComoAna();
            verify(conexion).alRecibirNotificacion(any());
            verify(conexion).conectar("localhost", 5000);
            assertThat(fachada.usuarioActual())
                    .isEqualTo(new UsuarioRemoto("1001", "Ana María", "Gómez Ruiz", "Ingeniería de Sistemas", true));
        }

        @Test
        @DisplayName("Si el servidor rechaza el LOGIN, cierra la conexión y deja ver el motivo")
        void loginRechazado() throws IOException {
            when(conexion.pedir("LOGIN", "1001", "mala")).thenThrow(new ErrorDelServidorException("credenciales inválidas"));
            assertThatThrownBy(() -> fachada.iniciarSesion("localhost", 5000, "1001", "mala"))
                    .isInstanceOf(IOException.class).hasMessage("credenciales inválidas");
            verify(conexion).cerrar();
            assertThat(fachada.usuarioActual()).isNull();
        }

        @Test
        @DisplayName("Sin sesión iniciada, enviar o leer el historial es un error de programación")
        void sinSesion() {
            assertThatThrownBy(() -> fachada.enviarTexto("1002", "hola")).isInstanceOf(IllegalStateException.class);
            assertThatThrownBy(() -> fachada.conversacion("1002")).isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("cerrarSesion envía SALIR y cierra; un CIERRE posterior ya no se avisa")
        void cerrarSesion() throws IOException {
            entrarComoAna();
            when(conexion.conectada()).thenReturn(true);
            fachada.cerrarSesion();
            verify(conexion).pedir("SALIR");
            verify(conexion).cerrar();
            fachada.atenderNotificacion(List.of("CIERRE", "se perdió la conexión con el servidor"));
            assertThat(observador.cierres).isEmpty();
        }

        @Test
        @DisplayName("cerrarSesion cierra aunque SALIR falle")
        void cerrarSesionConError() throws IOException {
            entrarComoAna();
            when(conexion.conectada()).thenReturn(true);
            when(conexion.pedir("SALIR")).thenThrow(new IOException("se perdió la conexión"));
            fachada.cerrarSesion();
            verify(conexion).cerrar();
        }

        @Test
        @DisplayName("filtrosDisponibles devuelve los de la configuración")
        void filtros() {
            assertThat(fachada.filtrosDisponibles()).containsExactly("sepia", "brillo");
        }
    }

    @Nested
    @DisplayName("Peticiones al servidor")
    class Peticiones {

        @BeforeEach
        void entrar() throws IOException {
            entrarComoAna();
        }

        @Test
        @DisplayName("listarUsuarios interpreta 5 campos por usuario")
        void listarUsuarios() throws IOException {
            when(conexion.pedir("USUARIOS")).thenReturn(List.of("2",
                    "1001", "Ana María", "Gómez Ruiz", "Sistemas", "1",
                    "1002", "Carlos", "Pérez", "Sistemas", "0"));
            assertThat(fachada.listarUsuarios()).containsExactly(
                    new UsuarioRemoto("1001", "Ana María", "Gómez Ruiz", "Sistemas", true),
                    new UsuarioRemoto("1002", "Carlos", "Pérez", "Sistemas", false));
        }

        @Test
        @DisplayName("Una respuesta de USUARIOS sin número es un error de protocolo")
        void listarUsuariosInvalido() throws IOException {
            when(conexion.pedir("USUARIOS")).thenReturn(List.of("muchos"));
            assertThatThrownBy(() -> fachada.listarUsuarios()).isInstanceOf(IOException.class)
                    .hasMessageContaining("USUARIOS");
        }

        @Test
        @DisplayName("enviarTexto envía TEXTO y guarda el mensaje en la conversación con el destino")
        void enviarTexto() throws IOException {
            when(conexion.pedir("TEXTO", "1002", "Hola | Carlos")).thenReturn(List.of("id-1"));
            MensajeChat m = fachada.enviarTexto("1002", "Hola | Carlos");
            assertThat(m.id()).isEqualTo("id-1");
            assertThat(m.contacto()).isEqualTo("1002");
            assertThat(m.enviadoPorMi()).isTrue();
            assertThat(fachada.conversacion("1002")).containsExactly(m);
        }

        @Test
        @DisplayName("Un mensaje a todos queda en la conversación '*'")
        void enviarATodos() throws IOException {
            when(conexion.pedir("TEXTO", "*", "Hola a todos")).thenReturn(List.of("id-2"));
            fachada.enviarTexto("*", "Hola a todos");
            assertThat(fachada.conversacion(UsuarioRemoto.TODOS)).hasSize(1);
        }

        @Test
        @DisplayName("Si el servidor rechaza el texto, no se guarda nada")
        void textoRechazado() throws IOException {
            when(conexion.pedir("TEXTO", "9999", "x")).thenThrow(new ErrorDelServidorException("el destino 9999 no es un usuario registrado"));
            assertThatThrownBy(() -> fachada.enviarTexto("9999", "x")).hasMessageContaining("9999");
            assertThat(historial.guardados).isEmpty();
        }

        @Test
        @DisplayName("enviarArchivo sin filtros envía 4 campos con el contenido en Base64")
        void archivoSinFiltros(@TempDir Path carpeta) throws IOException {
            Path archivo = Files.writeString(carpeta.resolve("notas.txt"), "hola", StandardCharsets.UTF_8);
            String base64 = Base64.getEncoder().encodeToString("hola".getBytes(StandardCharsets.UTF_8));
            when(conexion.pedir("ARCHIVO", "1002", "notas.txt", base64)).thenReturn(List.of("id-3"));
            MensajeChat m = fachada.enviarArchivo("1002", archivo);
            assertThat(m.tipo()).isEqualTo(TipoMensaje.ARCHIVO);
            assertThat(m.contenido()).isEqualTo("notas.txt");
            assertThat(Path.of(m.rutaArchivo())).startsWith(descargas.toAbsolutePath()).hasContent("hola");
            assertThat(historial.guardados).containsExactly(m);
        }

        @Test
        @DisplayName("enviarArchivo con filtros los envía en el quinto campo, en el orden elegido; una imagen queda como IMAGEN")
        void imagenConFiltros(@TempDir Path carpeta) throws IOException {
            byte[] datos = png();
            Path archivo = Files.write(carpeta.resolve("foto.png"), datos);
            String base64 = Base64.getEncoder().encodeToString(datos);
            when(conexion.pedir("ARCHIVO", "1002", "foto.png", base64, "brillo,sepia")).thenReturn(List.of("id-4"));
            MensajeChat m = fachada.enviarArchivo("1002", archivo, List.of("brillo", "sepia"));
            assertThat(m.tipo()).isEqualTo(TipoMensaje.IMAGEN);
            assertThat(Files.readAllBytes(Path.of(m.rutaArchivo()))).isEqualTo(datos);
        }

        @Test
        @DisplayName("Un archivo vacío o más grande que el máximo se rechaza sin contactar al servidor")
        void archivoInvalido(@TempDir Path carpeta) throws IOException {
            Path vacio = Files.write(carpeta.resolve("vacio.txt"), new byte[0]);
            Path grande = Files.write(carpeta.resolve("grande.bin"), new byte[1001]);
            assertThatThrownBy(() -> fachada.enviarArchivo("1002", vacio)).hasMessageContaining("vacío");
            assertThatThrownBy(() -> fachada.enviarArchivo("1002", grande)).hasMessageContaining("máximo");
            assertThat(mockingDetails(conexion).getInvocations())
                    .noneMatch(i -> i.getArguments().length > 0 && "ARCHIVO".equals(i.getArguments()[0]));
        }

        @Test
        @DisplayName("Si el historial falla, el mensaje enviado se devuelve igual")
        void historialFalla() throws IOException {
            IRepositorioMensajesLocal roto = mock(IRepositorioMensajesLocal.class);
            doThrow(new ErrorPersistencia("disco lleno", null)).when(roto).guardar(any());
            FachadaCliente otra = new FachadaCliente(conexion, roto, descargas, List.of(), 1000);
            otra.iniciarSesion("localhost", 5000, "1001", "clave1001");
            when(conexion.pedir("TEXTO", "1002", "hola")).thenReturn(List.of("id-5"));
            assertThat(otra.enviarTexto("1002", "hola").id()).isEqualTo("id-5");
        }
    }

    @Nested
    @DisplayName("Notificaciones del servidor")
    class Notificaciones {

        @BeforeEach
        void entrar() throws IOException {
            entrarComoAna();
        }

        private List<String> mensaje(String id, String origen, String destino, String tipo, String contenido,
                                     String nombre, String base64) {
            return List.of("MENSAJE", id, origen, destino, tipo, "2026-10-24T17:07:02.153", contenido, nombre, base64);
        }

        @Test
        @DisplayName("Un texto recibido se guarda en la conversación con el remitente y se avisa")
        void textoRecibido() throws InterruptedException {
            fachada.atenderNotificacion(mensaje("m1", "1002", "1001", "TEXTO", "Hola Ana", "mensaje.txt", ""));
            MensajeChat m = RegistroObservador.esperar(observador.mensajes);
            assertThat(m.contacto()).isEqualTo("1002");
            assertThat(m.contenido()).isEqualTo("Hola Ana");
            assertThat(m.fecha()).hasToString("2026-10-24T17:07:02.153");
            assertThat(m.enviadoPorMi()).isFalse();
            assertThat(historial.guardados).containsExactly(m);
        }

        @Test
        @DisplayName("Un mensaje para todos va a la conversación '*', también los del servidor")
        void paraTodos() throws InterruptedException {
            fachada.atenderNotificacion(mensaje("m2", "SERVIDOR", "*", "TEXTO", "Mantenimiento", "", ""));
            assertThat(RegistroObservador.esperar(observador.mensajes).contacto()).isEqualTo("*");
        }

        @Test
        @DisplayName("Un mensaje que uno se envía a sí mismo queda en esa conversación")
        void aMiMismo() throws InterruptedException {
            fachada.atenderNotificacion(mensaje("m3", "1001", "1001", "TEXTO", "nota", "", ""));
            assertThat(RegistroObservador.esperar(observador.mensajes).contacto()).isEqualTo("1001");
        }

        @Test
        @DisplayName("Un archivo recibido se guarda en descargas/<usuario>/<id>_<nombre> con los mismos bytes")
        void archivoRecibido() throws Exception {
            byte[] datos = png();
            fachada.atenderNotificacion(mensaje("m4", "1002", "1001", "IMAGEN", "", "foto.png",
                    Base64.getEncoder().encodeToString(datos)));
            MensajeChat m = RegistroObservador.esperar(observador.mensajes);
            Path ruta = Path.of(m.rutaArchivo());
            assertThat(ruta).isEqualTo(descargas.resolve("1001").resolve("m4_foto.png").toAbsolutePath());
            assertThat(Files.readAllBytes(ruta)).isEqualTo(datos);
            assertThat(m.contenido()).isEqualTo("foto.png");
        }

        @Test
        @DisplayName("Un nombre de archivo con ../ no puede escribir fuera de la carpeta de descargas")
        void nombrePeligroso() throws Exception {
            fachada.atenderNotificacion(mensaje("m5", "1002", "1001", "ARCHIVO", "", "../../../etc/passwd",
                    Base64.getEncoder().encodeToString(new byte[]{1})));
            Path ruta = Path.of(RegistroObservador.esperar(observador.mensajes).rutaArchivo());
            assertThat(ruta.getParent()).isEqualTo(descargas.resolve("1001").toAbsolutePath());
            assertThat(ruta.getFileName().toString()).doesNotContain("/").doesNotContain("\\");
        }

        @Test
        @DisplayName("Un mensaje mal formado se descarta sin avisar ni romper la escucha")
        void malFormado() {
            fachada.atenderNotificacion(mensaje("m6", "1002", "1001", "VIDEO", "", "x", ""));
            fachada.atenderNotificacion(mensaje("m7", "1002", "1001", "ARCHIVO", "", "x", "%%no-es-base64%%"));
            fachada.atenderNotificacion(List.of("DESCONOCIDA", "x"));
            assertThat(observador.mensajes).isEmpty();
            assertThat(historial.guardados).isEmpty();
        }

        @Test
        @DisplayName("PRESENCIA avisa el código y el estado")
        void presencia() throws InterruptedException {
            fachada.atenderNotificacion(List.of("PRESENCIA", "1002", "1"));
            fachada.atenderNotificacion(List.of("PRESENCIA", "1002", "0"));
            assertThat(RegistroObservador.esperar(observador.presencias)).isEqualTo("1002=true");
            assertThat(RegistroObservador.esperar(observador.presencias)).isEqualTo("1002=false");
        }

        @Test
        @DisplayName("CIERRE se avisa una sola vez aunque llegue dos veces, y cierra la conexión")
        void cierreUnaVez() throws InterruptedException {
            fachada.atenderNotificacion(List.of("CIERRE", "conexión cerrada por el administrador"));
            fachada.atenderNotificacion(List.of("CIERRE", "se perdió la conexión con el servidor"));
            assertThat(RegistroObservador.esperar(observador.cierres)).isEqualTo("conexión cerrada por el administrador");
            assertThat(observador.cierres).isEmpty();
            verify(conexion, atLeastOnce()).cerrar();
        }
    }

    @Nested
    @DisplayName("Reglas auxiliares")
    class Reglas {

        @Test
        @DisplayName("contactoDe: '*' si es para todos; si no, el otro usuario")
        void contactoDe() {
            assertThat(FachadaCliente.contactoDe("1001", "1002", "*")).isEqualTo("*");
            assertThat(FachadaCliente.contactoDe("1001", "1002", "1001")).isEqualTo("1002");
            assertThat(FachadaCliente.contactoDe("1001", "1001", "1002")).isEqualTo("1002");
        }

        @Test
        @DisplayName("nombreSeguro quita separadores de ruta y caracteres no válidos en Windows")
        void nombreSeguro() {
            assertThat(FachadaCliente.nombreSeguro("a/b\\c:d*e?f\"g<h>i|j.txt")).isEqualTo("a_b_c_d_e_f_g_h_i_j.txt");
            assertThat(FachadaCliente.nombreSeguro("..")).isEqualTo("archivo");
            assertThat(FachadaCliente.nombreSeguro("  ")).isEqualTo("archivo");
            assertThat(FachadaCliente.nombreSeguro("informe final.pdf")).isEqualTo("informe final.pdf");
        }

        @Test
        @DisplayName("esImagen reconoce una imagen por su contenido, no por el nombre")
        void esImagen() throws IOException {
            assertThat(FachadaCliente.esImagen(png())).isTrue();
            assertThat(FachadaCliente.esImagen("no soy una imagen".getBytes(StandardCharsets.UTF_8))).isFalse();
            assertThat(FachadaCliente.esImagen(new byte[0])).isFalse();
        }
    }
}
