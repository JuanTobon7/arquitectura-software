package co.edu.mensajeria.cliente.integracion;

import co.edu.mensajeria.cliente.dto.ConfiguracionCliente;
import co.edu.mensajeria.cliente.dto.MensajeChat;
import co.edu.mensajeria.cliente.dto.TipoMensaje;
import co.edu.mensajeria.cliente.dto.UsuarioRemoto;
import co.edu.mensajeria.cliente.fachada.FabricaCliente;
import co.edu.mensajeria.cliente.fachada.IFachadaCliente;
import co.edu.mensajeria.cliente.soporte.RegistroObservador;
import co.edu.mensajeria.cliente.soporte.ServidorDePrueba;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static co.edu.mensajeria.cliente.soporte.RegistroObservador.esperar;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * El cliente completo, armado por {@link FabricaCliente} (TCP real, H2 en archivo y descargas en
 * disco), contra un servidor que habla el protocolo. Dos usuarios conversan como en dos ventanas.
 */
@DisplayName("Extremo a extremo: dos clientes completos conversan por TCP")
class ClienteExtremoAExtremoTest {

    @TempDir
    Path carpeta;

    private ServidorDePrueba servidor;
    private ConfiguracionCliente configuracion;
    private final List<IFachadaCliente> abiertas = new ArrayList<>();

    @BeforeEach
    void arrancar() throws IOException {
        servidor = new ServidorDePrueba();
        configuracion = new ConfiguracionCliente("localhost", servidor.puerto(),
                "jdbc:h2:" + carpeta.resolve("historial") + ";AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1", "sa", "",
                carpeta.resolve("descargas").toString(), List.of("sepia", "brillo"), 1024 * 1024);
    }

    @AfterEach
    void detener() throws IOException {
        abiertas.forEach(IFachadaCliente::cerrarSesion);
        servidor.close();
    }

    private IFachadaCliente entrar(String codigo, RegistroObservador observador) throws IOException {
        IFachadaCliente fachada = FabricaCliente.crear(configuracion);
        fachada.agregarObservador(observador);
        fachada.iniciarSesion("localhost", servidor.puerto(), codigo, "clave" + codigo);
        abiertas.add(fachada);
        return fachada;
    }

    @Test
    @DisplayName("Ana ve cuando Carlos se conecta y la lista de usuarios lo muestra conectado")
    void presenciaYUsuarios() throws Exception {
        RegistroObservador deAna = new RegistroObservador();
        IFachadaCliente ana = entrar("1001", deAna);
        entrar("1002", new RegistroObservador());
        assertThat(esperar(deAna.presencias)).isEqualTo("1002=true");
        assertThat(ana.listarUsuarios()).filteredOn(UsuarioRemoto::conectado)
                .extracting(UsuarioRemoto::codigo).containsExactlyInAnyOrder("1001", "1002");
    }

    @Test
    @DisplayName("Un texto de Ana llega a Carlos y queda en el historial de los dos")
    void texto() throws Exception {
        IFachadaCliente ana = entrar("1001", new RegistroObservador());
        RegistroObservador deCarlos = new RegistroObservador();
        IFachadaCliente carlos = entrar("1002", deCarlos);
        MensajeChat enviado = ana.enviarTexto("1002", "Hola Carlos | ¿cómo vas?\nSegunda línea");
        MensajeChat recibido = esperar(deCarlos.mensajes);
        assertThat(recibido.id()).isEqualTo(enviado.id());
        assertThat(recibido.contenido()).isEqualTo("Hola Carlos | ¿cómo vas?\nSegunda línea");
        assertThat(recibido.contacto()).isEqualTo("1001");
        assertThat(ana.conversacion("1002")).extracting(MensajeChat::id).containsExactly(enviado.id());
        assertThat(carlos.conversacion("1001")).extracting(MensajeChat::id).containsExactly(enviado.id());
    }

    @Test
    @DisplayName("Una imagen con filtros elegidos llega idéntica y el servidor recibe los filtros en orden")
    void imagenConFiltros() throws Exception {
        IFachadaCliente ana = entrar("1001", new RegistroObservador());
        RegistroObservador deCarlos = new RegistroObservador();
        entrar("1002", deCarlos);
        Path foto = carpeta.resolve("foto.png");
        ImageIO.write(new BufferedImage(30, 20, BufferedImage.TYPE_INT_RGB), "png", foto.toFile());
        ana.enviarArchivo("1002", foto, List.of("brillo", "sepia"));
        MensajeChat recibido = esperar(deCarlos.mensajes);
        assertThat(recibido.tipo()).isEqualTo(TipoMensaje.IMAGEN);
        assertThat(Files.readAllBytes(Path.of(recibido.rutaArchivo()))).isEqualTo(Files.readAllBytes(foto));
        assertThat(servidor.recibidas).anyMatch(l -> l.startsWith("ARCHIVO|1002|foto.png|") && l.endsWith("|brillo,sepia"));
    }

    @Test
    @DisplayName("Un mensaje a todos llega a los demás en la conversación '*', y el del administrador también")
    void paraTodos() throws Exception {
        IFachadaCliente ana = entrar("1001", new RegistroObservador());
        RegistroObservador deCarlos = new RegistroObservador();
        RegistroObservador deLaura = new RegistroObservador();
        entrar("1002", deCarlos);
        entrar("1003", deLaura);
        ana.enviarTexto(UsuarioRemoto.TODOS, "Hola a todos");
        assertThat(esperar(deCarlos.mensajes).contacto()).isEqualTo("*");
        assertThat(esperar(deLaura.mensajes).contacto()).isEqualTo("*");
        servidor.difundir("Mantenimiento a las 6");
        MensajeChat delServidor = esperar(deCarlos.mensajes);
        assertThat(delServidor.origen()).isEqualTo("SERVIDOR");
        assertThat(delServidor.contacto()).isEqualTo("*");
    }

    @Test
    @DisplayName("Si el administrador cierra la sesión, el cliente recibe el aviso una vez con el motivo")
    void cierrePorElServidor() throws Exception {
        RegistroObservador deCarlos = new RegistroObservador();
        IFachadaCliente carlos = entrar("1002", deCarlos);
        servidor.cerrarSesionesDe("1002", "conexión cerrada por el administrador");
        assertThat(esperar(deCarlos.cierres)).isEqualTo("conexión cerrada por el administrador");
        Thread.sleep(300);
        assertThat(deCarlos.cierres).isEmpty();
        assertThatThrownBy(carlos::listarUsuarios).isInstanceOf(IOException.class);
    }

    @Test
    @DisplayName("Si el servidor se cae, el cliente se entera aunque no esté esperando nada")
    void servidorSeCae() throws Exception {
        RegistroObservador deAna = new RegistroObservador();
        entrar("1001", deAna);
        servidor.caer();
        assertThat(esperar(deAna.cierres)).contains("se perdió la conexión");
    }

    @Test
    @DisplayName("Clave incorrecta: el error del servidor llega tal cual")
    void claveIncorrecta() {
        IFachadaCliente fachada = FabricaCliente.crear(configuracion);
        assertThatThrownBy(() -> fachada.iniciarSesion("localhost", servidor.puerto(), "1001", "mala"))
                .isInstanceOf(IOException.class).hasMessage("credenciales inválidas");
    }

    @Test
    @DisplayName("El historial sigue ahí al iniciar sesión otra vez (cliente reiniciado)")
    void historialTrasReiniciar() throws Exception {
        IFachadaCliente ana = entrar("1001", new RegistroObservador());
        entrar("1002", new RegistroObservador());
        ana.enviarTexto("1002", "primero");
        ana.enviarTexto("1002", "segundo");
        ana.cerrarSesion();
        IFachadaCliente anaOtraVez = entrar("1001", new RegistroObservador());
        assertThat(anaOtraVez.conversacion("1002")).extracting(MensajeChat::contenido)
                .containsExactly("primero", "segundo");
    }
}
