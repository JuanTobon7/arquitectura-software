package com.plugandplay.core.registry;

import com.plugandplay.core.plugins.Plugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Gestor de componentes que permite descubrir plugins del classpath y cargar
 * manualmente JARs externos o carpetas completas en tiempo de ejecuciÃ³n.
 */
public class ComponentManager {

    private final PluginRegistry registry;

    public ComponentManager() {
        this.registry = new PluginRegistry();
    }

    public Map<String, Plugin> discoverPlugins() {
        return registry.discover();
    }

    public void installPluginJar(Path jarPath) {
        if (!Files.exists(jarPath) || !jarPath.toString().endsWith(".jar")) {
            throw new IllegalArgumentException("No es un JAR valido: " + jarPath);
        }
        registry.loadExternalJar(jarPath);
    }

    /**
     * Carga todos los JARs contenidos en una carpeta (no recursivo).
     */
    public void installPluginFolder(Path folder) {
        if (!Files.isDirectory(folder)) {
            throw new IllegalArgumentException("No es una carpeta valida: " + folder);
        }
        try (Stream<Path> jars = Files.list(folder)) {
            jars.filter(p -> p.toString().endsWith(".jar"))
                    .sorted()
                    .forEach(this::installPluginJar);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo leer la carpeta de plugins: " + folder, e);
        }
    }

    public void unloadExternalPlugins() {
        registry.closeExternalLoader();
    }
}
