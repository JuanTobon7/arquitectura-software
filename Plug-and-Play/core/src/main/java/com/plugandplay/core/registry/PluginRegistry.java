package com.plugandplay.core.registry;

import com.plugandplay.core.plugins.Plugin;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Registro de plugins que descubre implementaciones a travÃ©s del SPI de Java.
 *
 * Los plugins empaquetados en el classpath de la aplicaciÃ³n se detectan
 * automÃ¡ticamente. AdemÃ¡s, permite cargar manualmente JARs externos usando un
 * Ãºnico classloader compartido, de forma que los plugins manuales puedan ver
 * las clases unos de otros (por ejemplo, un plugin externo que extiende una
 * clase base de otro plugin).
 */
public final class PluginRegistry {

    private final PluginClassLoader externalLoader;

    public PluginRegistry() {
        this.externalLoader = new PluginClassLoader(PluginRegistry.class.getClassLoader());
    }

    public Map<String, Plugin> discover() {
        Map<String, Plugin> plugins = new LinkedHashMap<>();

        ServiceLoader.load(Plugin.class).forEach(p -> plugins.put(p.id(), p));
        ServiceLoader.load(Plugin.class, externalLoader)
                .forEach(p -> plugins.putIfAbsent(p.id(), p));

        return plugins;
    }

    public void loadExternalJar(Path jarPath) {
        if (!jarPath.toString().endsWith(".jar")) {
            throw new IllegalArgumentException("No es un JAR: " + jarPath);
        }
        try {
            externalLoader.addJar(jarPath.toUri().toURL());
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar el plugin: " + jarPath, e);
        }
    }

    public void closeExternalLoader() {
        try {
            externalLoader.close();
        } catch (IOException ignored) {
        }
    }

    /**
     * Classloader mutable que permite aÃ±adir JARs en tiempo de ejecuciÃ³n.
     */
    private static final class PluginClassLoader extends URLClassLoader {

        PluginClassLoader(ClassLoader parent) {
            super(new URL[0], parent);
        }

        void addJar(URL url) {
            addURL(url);
        }
    }
}
