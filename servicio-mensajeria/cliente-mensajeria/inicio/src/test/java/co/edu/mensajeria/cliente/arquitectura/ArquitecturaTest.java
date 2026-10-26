package co.edu.mensajeria.cliente.arquitectura;

import co.edu.mensajeria.cliente.soporte.FuentesDelProyecto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifica sobre el código fuente que cada componente use solo los componentes hacia los que
 * el diagrama "Cliente - Componentes" dibuja una flecha.
 */
@DisplayName("Arquitectura: dependencias del diagrama de componentes del cliente")
class ArquitecturaTest {

    private static final String BASE = "co.edu.mensajeria.cliente.";

    /** Componente → componentes que puede usar (además de sí mismo). Son las flechas del diagrama. */
    static final Map<String, Set<String>> PERMITIDAS = Map.of(
            "inicio", Set.of("utilerias", "vista", "fachada", "dto"),
            "vista", Set.of("fachada", "dto"),
            "utilerias", Set.of("dto"),
            "fachada", Set.of("comunicacion", "persistencia", "dto"),
            "comunicacion", Set.of(),
            "persistencia", Set.of("dto"),
            "dto", Set.of());

    private static final Pattern IMPORT = Pattern.compile("^import\\s+(?:static\\s+)?" + Pattern.quote(BASE)
            + "([a-z]+)\\.(\\w+)", Pattern.MULTILINE);
    private static final Pattern IMPORT_GRAFICO = Pattern.compile(
            "^import\\s+(javax\\.swing|java\\.awt(?!\\.image\\.))[\\w.]*", Pattern.MULTILINE);

    @Test
    @DisplayName("Cada componente depende solo de los componentes a los que apunta en el diagrama")
    void dependenciasDelDiagrama() throws IOException {
        List<String> violaciones = new ArrayList<>();
        Set<String> componentes = new TreeSet<>();
        for (Path archivo : FuentesDelProyecto.archivosMain()) {
            String origen = FuentesDelProyecto.componente(archivo);
            componentes.add(origen);
            Set<String> permitidas = PERMITIDAS.getOrDefault(origen, Set.of());
            Matcher m = IMPORT.matcher(Files.readString(archivo, StandardCharsets.UTF_8));
            while (m.find()) {
                String destino = m.group(1);
                if (!destino.equals(origen) && !permitidas.contains(destino)) {
                    violaciones.add(archivo.getFileName() + " (" + origen + ") → " + destino + "." + m.group(2));
                }
            }
        }
        assertThat(componentes).as("los 7 componentes del diagrama").isEqualTo(PERMITIDAS.keySet());
        assertThat(violaciones).isEmpty();
    }

    @Test
    @DisplayName("Cada componente está en su propio módulo Maven (un jar por componente)")
    void unModuloPorComponente() {
        List<String> fueraDeLugar = FuentesDelProyecto.archivosMain().stream()
                .filter(a -> !FuentesDelProyecto.modulo(a).equals(FuentesDelProyecto.componente(a)))
                .map(a -> a.getFileName() + " está en el módulo " + FuentesDelProyecto.modulo(a))
                .toList();
        assertThat(fueraDeLugar).isEmpty();
    }

    @Test
    @DisplayName("Solo FabricaCliente conoce las implementaciones de la comunicación y la persistencia")
    void soloLaFabricaConoceImplementaciones() throws IOException {
        List<String> usos = new ArrayList<>();
        for (Path archivo : FuentesDelProyecto.archivosMain()) {
            String nombre = archivo.getFileName().toString();
            String codigo = Files.readString(archivo, StandardCharsets.UTF_8);
            boolean usa = codigo.contains("ConexionServidorTCP") || codigo.contains("RepositorioMensajesH2");
            boolean esLaImplementacion = nombre.equals("ConexionServidorTCP.java")
                    || nombre.equals("RepositorioMensajesH2.java");
            if (usa && !esLaImplementacion && !nombre.equals("FabricaCliente.java")) {
                usos.add(nombre);
            }
        }
        assertThat(usos).isEmpty();
    }

    @Test
    @DisplayName("Swing y AWT solo se usan en la vista y en el arranque")
    void interfazGraficaSoloEnLaVista() throws IOException {
        List<String> graficas = new ArrayList<>();
        for (Path archivo : FuentesDelProyecto.archivosMain()) {
            String componente = FuentesDelProyecto.componente(archivo);
            if (componente.equals("vista") || componente.equals("inicio")) {
                continue;
            }
            Matcher m = IMPORT_GRAFICO.matcher(Files.readString(archivo, StandardCharsets.UTF_8));
            while (m.find()) {
                graficas.add(archivo.getFileName() + ": " + m.group());
            }
        }
        assertThat(graficas).isEmpty();
    }

    @Test
    @DisplayName("Los pom.xml declaran las mismas dependencias entre componentes que el diagrama")
    void pomsSegunElDiagrama() throws IOException {
        Pattern dependencia = Pattern.compile("<artifactId>cliente-([a-z]+)</artifactId>");
        for (String componente : PERMITIDAS.keySet()) {
            String pom = Files.readString(FuentesDelProyecto.RAIZ.resolve(componente).resolve("pom.xml"));
            String dependencias = pom.substring(pom.indexOf("<dependencies>") < 0 ? pom.length()
                    : pom.indexOf("<dependencies>"));
            Set<String> declaradas = new TreeSet<>();
            Matcher m = dependencia.matcher(dependencias);
            while (m.find()) {
                declaradas.add(m.group(1));
            }
            Set<String> esperadas = new TreeSet<>(PERMITIDAS.get(componente));
            if (componente.equals("inicio")) {
                esperadas.addAll(Set.of("comunicacion", "persistencia")); // solo en tiempo de ejecución
            }
            assertThat(declaradas).as("dependencias de " + componente + "/pom.xml").isEqualTo(esperadas);
        }
    }
}
