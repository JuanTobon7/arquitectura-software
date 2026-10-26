package co.edu.mensajeria.cliente.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("DTO: records del cliente")
class DtoTest {

    @Test
    @DisplayName("UsuarioRemoto.todos() es el contacto '*' y siempre está conectado")
    void contactoTodos() {
        UsuarioRemoto todos = UsuarioRemoto.todos();
        assertThat(todos.codigo()).isEqualTo(UsuarioRemoto.TODOS).isEqualTo("*");
        assertThat(todos.conectado()).isTrue();
        assertThat(todos.nombreCompleto()).isEqualTo("Todos");
    }

    @Test
    @DisplayName("nombreCompleto une nombres y apellidos; conConexion cambia solo el estado")
    void usuario() {
        UsuarioRemoto ana = new UsuarioRemoto("1001", "Ana María", "Gómez Ruiz", "Sistemas", false);
        assertThat(ana.nombreCompleto()).isEqualTo("Ana María Gómez Ruiz");
        assertThat(ana.conConexion(true)).isEqualTo(new UsuarioRemoto("1001", "Ana María", "Gómez Ruiz", "Sistemas", true));
    }

    @Test
    @DisplayName("enviadoPorMi compara el origen con el propietario del historial")
    void enviadoPorMi() {
        LocalDateTime ahora = LocalDateTime.now();
        assertThat(new MensajeChat("1", "1001", "1002", "1001", "1002", TipoMensaje.TEXTO, ahora, "x", null)
                .enviadoPorMi()).isTrue();
        assertThat(new MensajeChat("1", "1002", "1001", "1001", "1002", TipoMensaje.TEXTO, ahora, "x", null)
                .enviadoPorMi()).isFalse();
    }

    @Test
    @DisplayName("ConfiguracionCliente copia la lista de filtros: no cambia si cambia la original")
    void configuracionInmutable() {
        List<String> filtros = new ArrayList<>(List.of("sepia"));
        ConfiguracionCliente c = new ConfiguracionCliente("h", 1, "u", "s", "", "d", filtros, 1);
        filtros.add("brillo");
        assertThat(c.filtrosDisponibles()).containsExactly("sepia");
        assertThatThrownBy(() -> c.filtrosDisponibles().add("x")).isInstanceOf(UnsupportedOperationException.class);
        assertThat(new ConfiguracionCliente("h", 1, "u", "s", "", "d", null, 1).filtrosDisponibles()).isEmpty();
    }
}
