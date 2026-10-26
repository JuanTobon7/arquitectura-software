package co.edu.mensajeria.cliente.calidad;

import co.edu.mensajeria.cliente.soporte.AnalizadorFuentes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Métricas de calidad sobre todo {@code src/main/java}, con los mismos umbrales que usa el
 * servidor (los usuales en SonarQube y Checkstyle).
 */
@DisplayName("Calidad de software: métricas del código fuente")
class MetricasCalidadTest {

    static final int MAX_LINEAS_METODO = 50;
    static final int MAX_COMPLEJIDAD = 10;
    static final int MAX_PARAMETROS = 7;
    static final int MAX_LINEAS_CLASE = 300;
    static final int MAX_METODOS_PUBLICOS = 15;

    private final AnalizadorFuentes fuentes = AnalizadorFuentes.instancia();

    @Test
    @DisplayName("Se analizó todo el código fuente")
    void seAnalizoElCodigo() {
        assertThat(fuentes.tipos()).hasSizeGreaterThan(20);
        assertThat(fuentes.metodos()).hasSizeGreaterThan(100);
    }

    @Test
    @DisplayName("Ningún método ni constructor supera " + MAX_LINEAS_METODO + " líneas")
    void metodosCortos() {
        List<String> largos = fuentes.metodos().stream()
                .filter(m -> m.lineas() > MAX_LINEAS_METODO)
                .map(m -> m + " (" + m.lineas() + " líneas)").toList();
        assertThat(largos).isEmpty();
    }

    @Test
    @DisplayName("Complejidad ciclomática de cada método ≤ " + MAX_COMPLEJIDAD)
    void complejidadAcotada() {
        List<String> complejos = fuentes.metodos().stream()
                .filter(m -> m.complejidad() > MAX_COMPLEJIDAD)
                .map(m -> m + " (complejidad " + m.complejidad() + ")").toList();
        assertThat(complejos).isEmpty();
    }

    @Test
    @DisplayName("Ningún método recibe más de " + MAX_PARAMETROS + " parámetros (salvo los records de datos)")
    void pocosParametros() {
        List<String> excedidos = fuentes.metodos().stream()
                .filter(m -> m.parametros() > MAX_PARAMETROS && !m.deRecord())
                .map(m -> m + " (" + m.parametros() + " parámetros)").toList();
        assertThat(excedidos).isEmpty();
    }

    @Test
    @DisplayName("Ninguna clase supera " + MAX_LINEAS_CLASE + " líneas")
    void clasesPequenas() {
        List<String> grandes = fuentes.tipos().stream()
                .filter(t -> t.lineas() > MAX_LINEAS_CLASE)
                .map(t -> t.nombre() + " (" + t.lineas() + " líneas)").toList();
        assertThat(grandes).isEmpty();
    }

    @Test
    @DisplayName("Ninguna clase o interfaz expone más de " + MAX_METODOS_PUBLICOS + " métodos públicos")
    void interfacesPublicasAcotadas() {
        List<String> anchas = fuentes.tipos().stream()
                .filter(t -> t.metodosPublicos() > MAX_METODOS_PUBLICOS)
                .map(t -> t.nombre() + " (" + t.metodosPublicos() + ")").toList();
        assertThat(anchas).isEmpty();
    }

    @Test
    @DisplayName("No se usa instanceof (se prefiere polimorfismo)")
    void sinInstanceOf() {
        assertThat(fuentes.usosDeInstanceOf()).isEmpty();
    }
}
