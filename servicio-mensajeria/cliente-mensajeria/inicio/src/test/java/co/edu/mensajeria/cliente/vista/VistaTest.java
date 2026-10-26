package co.edu.mensajeria.cliente.vista;

import co.edu.mensajeria.cliente.dto.MensajeChat;
import co.edu.mensajeria.cliente.dto.TipoMensaje;
import co.edu.mensajeria.cliente.dto.UsuarioRemoto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/** Lo que se puede probar de la vista sin pantalla: renderizadores, selector de filtros y utilidades. */
@DisplayName("Vista (sin pantalla)")
class VistaTest {

    private static MensajeChat mensaje(String origen, String contacto, TipoMensaje tipo, String contenido, String ruta) {
        return new MensajeChat("id", "1001", contacto, origen, "1002", tipo, LocalDateTime.now(), contenido, ruta);
    }

    /** Todos los componentes dentro de uno, recorriendo el árbol. */
    private static List<Component> componentes(Component raiz) {
        List<Component> todos = new ArrayList<>(List.of(raiz));
        if (raiz instanceof Container c) {
            for (Component hijo : c.getComponents()) {
                todos.addAll(componentes(hijo));
            }
        }
        return todos;
    }

    @Nested
    @DisplayName("RenderizadorMensaje")
    class Renderizador {

        private final RenderizadorMensaje renderizador = new RenderizadorMensaje(c -> "Nombre de " + c);

        private Component pintar(MensajeChat m) {
            return renderizador.getListCellRendererComponent(new JList<>(), m, 0, false, false);
        }

        @Test
        @DisplayName("Los mensajes propios van a la derecha y los recibidos a la izquierda")
        void alineacion() {
            JPanel propio = (JPanel) pintar(mensaje("1001", "1002", TipoMensaje.TEXTO, "hola", null));
            JPanel ajeno = (JPanel) pintar(mensaje("1002", "1002", TipoMensaje.TEXTO, "hola", null));
            assertThat(((FlowLayout) propio.getLayout()).getAlignment()).isEqualTo(FlowLayout.RIGHT);
            assertThat(((FlowLayout) ajeno.getLayout()).getAlignment()).isEqualTo(FlowLayout.LEFT);
        }

        @Test
        @DisplayName("En la conversación con todos, un mensaje recibido muestra quién lo envió")
        void remitenteEnTodos() {
            Component celda = pintar(mensaje("1002", UsuarioRemoto.TODOS, TipoMensaje.TEXTO, "hola", null));
            assertThat(componentes(celda)).filteredOn(c -> c instanceof JLabel)
                    .extracting(c -> ((JLabel) c).getText()).contains("Nombre de 1002");
        }

        @Test
        @DisplayName("El texto largo se ajusta al ancho máximo; el corto ocupa solo lo que necesita")
        void ajusteDeTexto() {
            JTextArea corto = RenderizadorMensaje.textoAjustado("hola");
            JTextArea largo = RenderizadorMensaje.textoAjustado("palabra ".repeat(80));
            assertThat(corto.getPreferredSize().width).isLessThan(RenderizadorMensaje.ANCHO_TEXTO);
            assertThat(largo.getPreferredSize().width).isEqualTo(RenderizadorMensaje.ANCHO_TEXTO);
            assertThat(largo.getPreferredSize().height).isGreaterThan(3 * corto.getPreferredSize().height);
            assertThat(largo.getMaximumSize()).isEqualTo(largo.getPreferredSize());
        }

        @Test
        @DisplayName("Una imagen se muestra en miniatura (máximo 220 px) y se lee una sola vez")
        void miniatura(@TempDir Path carpeta) throws IOException {
            Path ruta = carpeta.resolve("grande.png");
            ImageIO.write(new BufferedImage(880, 440, BufferedImage.TYPE_INT_RGB), "png", ruta.toFile());
            ImageIcon icono = renderizador.miniatura(ruta.toString());
            assertThat(icono.getIconWidth()).isEqualTo(RenderizadorMensaje.LADO_MINIATURA);
            assertThat(icono.getIconHeight()).isEqualTo(RenderizadorMensaje.LADO_MINIATURA / 2);
            assertThat(renderizador.miniatura(ruta.toString())).isSameAs(icono);
        }

        @Test
        @DisplayName("Si la imagen ya no existe, se muestra como archivo con su nombre")
        void imagenPerdida() {
            Component celda = pintar(mensaje("1002", "1002", TipoMensaje.IMAGEN, "foto.png", "/no/existe/foto.png"));
            assertThat(componentes(celda)).filteredOn(c -> c instanceof JLabel)
                    .extracting(c -> ((JLabel) c).getText()).anyMatch(t -> t != null && t.contains("foto.png"));
        }

        @Test
        @DisplayName("El nombre de un archivo se escapa para HTML")
        void escapaHtml() {
            assertThat(RenderizadorMensaje.html("<b>a & b</b>\nc")).isEqualTo("<html>&lt;b&gt;a &amp; b&lt;/b&gt;<br>c</html>");
        }
    }

    @Nested
    @DisplayName("SelectorFiltros")
    class Selector {

        @Test
        @DisplayName("Los filtros quedan en el orden en que se marcan; desmarcar los quita")
        void ordenDeMarcado() {
            SelectorFiltros selector = new SelectorFiltros(List.of("escala_grises", "sepia", "brillo"));
            assertThat(selector.textoOrden()).isEqualTo(SelectorFiltros.SIN_ELEGIR);
            selector.marcar("brillo", true);
            selector.marcar("escala_grises", true);
            selector.marcar("sepia", true);
            selector.marcar("escala_grises", false);
            assertThat(selector.elegidos()).containsExactly("brillo", "sepia");
            assertThat(selector.textoOrden()).isEqualTo("Orden: Brillo → Sepia");
        }

        @Test
        @DisplayName("Nombres legibles para los filtros conocidos y para los plugins nuevos")
        void nombresLegibles() {
            assertThat(SelectorFiltros.legible("girar_180")).isEqualTo("Girar 180°");
            assertThat(SelectorFiltros.legible("reduccion")).isEqualTo("Reducción");
            assertThat(SelectorFiltros.legible("negativo_suave")).isEqualTo("Negativo suave");
        }
    }

    @Nested
    @DisplayName("Utilidades")
    class Utilidades {

        @Test
        @DisplayName("El contacto con mensajes sin leer aparece en negrita y con el aviso 'nuevo'")
        void contactoNoLeido() {
            UsuarioRemoto carlos = new UsuarioRemoto("1002", "Carlos", "Pérez", "Sistemas", true);
            assertThat(RenderizadorContacto.texto(carlos, true)).contains("<b>Carlos Pérez</b>").contains("nuevo");
            assertThat(RenderizadorContacto.texto(carlos, false)).doesNotContain("<b>").doesNotContain("nuevo")
                    .contains("1002 · Sistemas");
            assertThat(RenderizadorContacto.texto(UsuarioRemoto.todos(), false)).contains("todos los conectados");
        }

        @Test
        @DisplayName("Solo se piden filtros para archivos con extensión de imagen")
        void extensionDeImagen() {
            assertThat(VentanaChat.esImagen(Path.of("Foto.JPG"))).isTrue();
            assertThat(VentanaChat.esImagen(Path.of("a.png"))).isTrue();
            assertThat(VentanaChat.esImagen(Path.of("informe.pdf"))).isFalse();
            assertThat(VentanaChat.esImagen(Path.of("sin-extension"))).isFalse();
        }

        @Test
        @DisplayName("El motivo que ve el usuario empieza con mayúscula; sin mensaje, el tipo de error")
        void motivo() {
            assertThat(SegundoPlano.motivo(new IOException("credenciales inválidas"))).isEqualTo("Credenciales inválidas");
            assertThat(SegundoPlano.motivo(new IllegalStateException())).isEqualTo("IllegalStateException");
        }

        @Test
        @DisplayName("SegundoPlano trabaja fuera del hilo de Swing y responde dentro de él")
        void segundoPlano() throws Exception {
            SegundoPlano plano = new SegundoPlano("prueba");
            CompletableFuture<String> resultado = new CompletableFuture<>();
            plano.ejecutar(() -> SwingUtilities.isEventDispatchThread() ? "en Swing" : "fuera",
                    r -> resultado.complete(r + (SwingUtilities.isEventDispatchThread() ? " / vuelve a Swing" : "")),
                    resultado::complete);
            assertThat(resultado.get(5, TimeUnit.SECONDS)).isEqualTo("fuera / vuelve a Swing");
            CompletableFuture<String> error = new CompletableFuture<>();
            plano.ejecutar(() -> {
                throw new IOException("sin conexión");
            }, r -> error.complete("no debía terminar"), error::complete);
            assertThat(error.get(5, TimeUnit.SECONDS)).isEqualTo("Sin conexión");
            plano.detener();
        }
    }
}
