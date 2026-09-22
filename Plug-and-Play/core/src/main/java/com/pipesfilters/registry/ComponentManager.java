package com.pipesfilters.registry;

import com.pipesfilters.plugins.Plugin;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Gestor de componentes que descubre plugins del classpath y permite
 * añadir dinámicamente JARs externos en tiempo de ejecución.
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
            throw new IllegalArgumentException(
                    "No es un JAR válido: " + jarPath
            );
        }
        registry.loadExternalJar(jarPath);
    }

    public void unloadExternalPlugins() {
        registry.closeExternalLoaders();
    }
}
