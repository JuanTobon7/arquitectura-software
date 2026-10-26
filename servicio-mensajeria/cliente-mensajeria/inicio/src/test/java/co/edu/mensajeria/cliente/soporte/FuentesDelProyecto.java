package co.edu.mensajeria.cliente.soporte;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * Ubica el código de todos los módulos del cliente para las pruebas estáticas (arquitectura y
 * calidad), que revisan el proyecto entero y no solo el módulo donde corren.
 */
public final class FuentesDelProyecto {

    /** Carpeta raíz del cliente: la que contiene {@code inicio/pom.xml}. */
    public static final Path RAIZ = buscarRaiz();

    private static final String BASE = "co/edu/mensajeria/cliente/";
    private static final String MAIN = "/src/main/java/";

    private FuentesDelProyecto() {
    }

    private static Path buscarRaiz() {
        for (Path p = Path.of("").toAbsolutePath(); p != null; p = p.getParent()) {
            if (Files.isRegularFile(p.resolve("inicio").resolve("pom.xml"))) {
                return p;
            }
        }
        throw new IllegalStateException("No se encontró la raíz del cliente desde " + Path.of("").toAbsolutePath());
    }

    /** Todos los {@code .java} de {@code src/main/java} de todos los módulos. */
    public static List<Path> archivosMain() {
        try (Stream<Path> rutas = Files.walk(RAIZ)) {
            return rutas.filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> texto(p).contains(MAIN))
                    .filter(p -> !texto(p).contains("/target/"))
                    .sorted().toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Módulo Maven del archivo (por ejemplo {@code fachada}). */
    public static String modulo(Path archivo) {
        String relativo = texto(RAIZ.relativize(archivo));
        return relativo.substring(0, relativo.indexOf(MAIN));
    }

    /** Componente del archivo: el paquete bajo {@code co.edu.mensajeria.cliente} (por ejemplo {@code vista}). */
    public static String componente(Path archivo) {
        String ruta = texto(archivo);
        String resto = ruta.substring(ruta.indexOf(BASE) + BASE.length());
        return resto.substring(0, resto.indexOf('/'));
    }

    private static String texto(Path p) {
        return p.toString().replace('\\', '/');
    }
}
