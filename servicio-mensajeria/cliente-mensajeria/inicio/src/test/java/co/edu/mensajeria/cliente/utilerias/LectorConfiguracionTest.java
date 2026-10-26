package co.edu.mensajeria.cliente.utilerias;

import co.edu.mensajeria.cliente.dto.ConfiguracionCliente;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("LectorConfiguracion: cliente.properties")
class LectorConfiguracionTest {

    @Test
    @DisplayName("Sin archivo se usan los valores por defecto")
    void valoresPorDefecto(@TempDir Path carpeta) throws IOException {
        ConfiguracionCliente c = LectorConfiguracion.leer(carpeta.resolve("no-existe.properties"));
        assertThat(c.host()).isEqualTo("localhost");
        assertThat(c.puerto()).isEqualTo(5000);
        assertThat(c.bdUrl()).isEqualTo(LectorConfiguracion.BD_POR_DEFECTO);
        assertThat(c.directorioDescargas()).isEqualTo("datos/descargas");
        assertThat(c.filtrosDisponibles()).isEqualTo(LectorConfiguracion.FILTROS_POR_DEFECTO);
        assertThat(c.tamanoMaximoBytes()).isEqualTo(10 * 1024 * 1024);
    }

    @Test
    @DisplayName("Lee todas las claves del archivo (UTF-8)")
    void leeTodasLasClaves(@TempDir Path carpeta) throws IOException {
        Path archivo = carpeta.resolve("cliente.properties");
        Files.writeString(archivo, """
                servidor.host = 192.168.1.20
                servidor.puerto = 6000
                bd.url = jdbc:h2:mem:x
                bd.usuario = yo
                bd.contrasena = secreta
                descargas.directorio = C:/Descargas/Mensajería
                filtros.disponibles = sepia, brillo
                archivos.tamanoMaximoBytes = 2048
                """, StandardCharsets.UTF_8);
        ConfiguracionCliente c = LectorConfiguracion.leer(archivo);
        assertThat(c).isEqualTo(new ConfiguracionCliente("192.168.1.20", 6000, "jdbc:h2:mem:x", "yo", "secreta",
                "C:/Descargas/Mensajería", java.util.List.of("sepia", "brillo"), 2048));
    }

    @Test
    @DisplayName("Los filtros se normalizan: sin espacios, en minúscula, sin vacíos ni repetidos")
    void normalizaFiltros() {
        Properties p = new Properties();
        p.setProperty("filtros.disponibles", " Sepia ,,BRILLO, sepia ,");
        assertThat(LectorConfiguracion.desdePropiedades(p).filtrosDisponibles()).containsExactly("sepia", "brillo");
    }

    @Test
    @DisplayName("Con filtros.disponibles vacío no se ofrece ningún filtro")
    void sinFiltros() {
        Properties p = new Properties();
        p.setProperty("filtros.disponibles", "");
        assertThat(LectorConfiguracion.desdePropiedades(p).filtrosDisponibles()).isEmpty();
    }

    @ParameterizedTest(name = "servidor.puerto={0}")
    @ValueSource(strings = {"cinco mil", "0", "70000", "-1"})
    @DisplayName("Un puerto inválido se rechaza con un mensaje que nombra la clave")
    void puertoInvalido(String puerto) {
        Properties p = new Properties();
        p.setProperty("servidor.puerto", puerto);
        assertThatThrownBy(() -> LectorConfiguracion.desdePropiedades(p))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("servidor.puerto");
    }

    @Test
    @DisplayName("Un tamaño máximo no positivo se rechaza")
    void tamanoInvalido() {
        Properties p = new Properties();
        p.setProperty("archivos.tamanoMaximoBytes", "0");
        assertThatThrownBy(() -> LectorConfiguracion.desdePropiedades(p))
                .hasMessageContaining("archivos.tamanoMaximoBytes");
    }

    @Test
    @DisplayName("El config/cliente.properties del proyecto es válido y coincide con los valores por defecto")
    void archivoDelProyecto() throws IOException {
        Path archivo = co.edu.mensajeria.cliente.soporte.FuentesDelProyecto.RAIZ.resolve("config/cliente.properties");
        ConfiguracionCliente delArchivo = LectorConfiguracion.leer(archivo);
        ConfiguracionCliente porDefecto = LectorConfiguracion.desdePropiedades(new Properties());
        assertThat(delArchivo).isEqualTo(porDefecto);
    }
}
