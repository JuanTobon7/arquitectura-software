package co.edu.mensajeria.cliente.persistencia;

import co.edu.mensajeria.cliente.dto.MensajeChat;
import co.edu.mensajeria.cliente.dto.TipoMensaje;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("RepositorioMensajesH2: historial local")
class RepositorioMensajesH2Test {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 10, 24, 17, 0, 0);

    private final RepositorioMensajesH2 repositorio = new RepositorioMensajesH2(
            "jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", "sa", "");

    private static MensajeChat mensaje(String id, String propietario, String contacto, int segundos) {
        return new MensajeChat(id, propietario, contacto, propietario, contacto, TipoMensaje.TEXTO,
                T0.plusSeconds(segundos), "texto " + id, null);
    }

    @Test
    @DisplayName("Devuelve la conversación ordenada por fecha, aunque se guarde desordenada")
    void ordenadaPorFecha() {
        repositorio.guardar(mensaje("b", "1001", "1002", 20));
        repositorio.guardar(mensaje("a", "1001", "1002", 10));
        repositorio.guardar(mensaje("c", "1001", "1002", 30));
        assertThat(repositorio.conversacion("1001", "1002")).extracting(MensajeChat::id).containsExactly("a", "b", "c");
    }

    @Test
    @DisplayName("Cada conversación es de un propietario y un contacto")
    void separaPorPropietarioYContacto() {
        repositorio.guardar(mensaje("1", "1001", "1002", 1));
        repositorio.guardar(mensaje("2", "1001", "1003", 2));
        repositorio.guardar(mensaje("3", "1002", "1001", 3));
        repositorio.guardar(mensaje("4", "1001", "*", 4));
        assertThat(repositorio.conversacion("1001", "1002")).extracting(MensajeChat::id).containsExactly("1");
        assertThat(repositorio.conversacion("1001", "*")).extracting(MensajeChat::id).containsExactly("4");
        assertThat(repositorio.conversacion("1002", "1001")).extracting(MensajeChat::id).containsExactly("3");
        assertThat(repositorio.conversacion("1003", "1001")).isEmpty();
    }

    @Test
    @DisplayName("Guardar dos veces el mismo mensaje no lo duplica")
    void sinDuplicados() {
        repositorio.guardar(mensaje("x", "1001", "1002", 1));
        repositorio.guardar(mensaje("x", "1001", "1002", 1));
        assertThat(repositorio.conversacion("1001", "1002")).hasSize(1);
    }

    @Test
    @DisplayName("El mismo id con otro propietario es otra copia (dos usuarios en el mismo equipo)")
    void mismoIdOtroPropietario() {
        repositorio.guardar(mensaje("x", "1001", "1002", 1));
        repositorio.guardar(mensaje("x", "1002", "1001", 1));
        assertThat(repositorio.conversacion("1001", "1002")).hasSize(1);
        assertThat(repositorio.conversacion("1002", "1001")).hasSize(1);
    }

    @Test
    @DisplayName("Se conservan todos los campos, incluida la ruta del archivo y los textos largos con símbolos")
    void conservaLosCampos() {
        String largo = "línea | con barra\n".repeat(2000);
        MensajeChat imagen = new MensajeChat("i", "1002", "1001", "1001", "1002", TipoMensaje.IMAGEN,
                T0.plusNanos(123_000_000), "foto.png", "/tmp/descargas/1002/i_foto.png");
        MensajeChat texto = new MensajeChat("t", "1002", "1001", "1001", "1002", TipoMensaje.TEXTO, T0.plusSeconds(1),
                largo, null);
        repositorio.guardar(imagen);
        repositorio.guardar(texto);
        assertThat(repositorio.conversacion("1002", "1001")).containsExactly(imagen, texto);
    }

    @Test
    @DisplayName("El historial sobrevive a cerrar y abrir el cliente (base en archivo)")
    void persisteEnArchivo(@TempDir Path carpeta) {
        String url = "jdbc:h2:" + carpeta.resolve("historial") + ";AUTO_SERVER=TRUE";
        new RepositorioMensajesH2(url, "sa", "").guardar(mensaje("p", "1001", "1002", 1));
        RepositorioMensajesH2 otraVez = new RepositorioMensajesH2(url, "sa", "");
        assertThat(otraVez.conversacion("1001", "1002")).extracting(MensajeChat::id).containsExactly("p");
    }

    @Test
    @DisplayName("Si la base no se puede abrir, lanza ErrorPersistencia con la URL")
    void urlInvalida() {
        assertThatThrownBy(() -> new RepositorioMensajesH2("jdbc:noexiste:x", "sa", ""))
                .isInstanceOf(ErrorPersistencia.class).hasMessageContaining("jdbc:noexiste:x");
    }
}
