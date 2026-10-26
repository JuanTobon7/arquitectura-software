package co.edu.mensajeria.cliente.comunicacion;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Protocolo: formato de línea compatible con el servidor")
class ProtocoloTest {

    @Test
    @DisplayName("Une los campos con '|' y escapa '|', '\\', saltos de línea y retornos")
    void uneYEscapa() {
        assertThat(Protocolo.unir("TEXTO", "1002", "a|b\\c\nd\re")).isEqualTo("TEXTO|1002|a\\|b\\\\c\\nd\\re");
    }

    @Test
    @DisplayName("Un campo null se envía vacío")
    void campoNulo() {
        assertThat(Protocolo.unir("A", null, "B")).isEqualTo("A||B");
    }

    @Test
    @DisplayName("Separa una línea del servidor deshaciendo el escape")
    void separa() {
        assertThat(Protocolo.separar("NOTIF|MENSAJE|id|1001|1002|TEXTO|f|hola\\|mundo\\nadiós||"))
                .containsExactly("NOTIF", "MENSAJE", "id", "1001", "1002", "TEXTO", "f", "hola|mundo\nadiós", "", "");
    }

    @Test
    @DisplayName("Una línea vacía es un solo campo vacío; nunca devuelve una lista vacía")
    void lineaVacia() {
        assertThat(Protocolo.separar("")).containsExactly("");
        assertThat(Protocolo.separar("OK")).containsExactly("OK");
    }

    @Test
    @DisplayName("Una barra invertida al final de la línea se conserva")
    void escapeAlFinal() {
        assertThat(Protocolo.separar("a\\")).containsExactly("a\\");
    }

    @ParameterizedTest(name = "ida y vuelta: {0}")
    @ValueSource(strings = {"texto simple", "con | barra", "con \\ invertida", "dos\nlíneas", "fin\\", "\\n literal",
            "ñandú, tildes y emoji 😀", "|||", ""})
    @DisplayName("separar(unir(x)) devuelve x para cualquier texto")
    void idaYVuelta(String texto) {
        assertThat(Protocolo.separar(Protocolo.unir("TEXTO", texto, "fin"))).containsExactly("TEXTO", texto, "fin");
    }

    @Test
    @DisplayName("El ejemplo de ARCHIVO del README del servidor tiene 5 campos")
    void ejemploArchivo() {
        List<String> campos = Protocolo.separar("ARCHIVO|1002|foto.png|iVBORw0KGgo=|brillo,sepia");
        assertThat(campos).containsExactly("ARCHIVO", "1002", "foto.png", "iVBORw0KGgo=", "brillo,sepia");
    }
}
