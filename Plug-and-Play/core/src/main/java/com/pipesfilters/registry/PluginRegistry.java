package com.pipesfilters.registry;

import com.pipesfilters.plugins.Plugin;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

public final class PluginRegistry {

    private final List<URLClassLoader> externalLoaders = new ArrayList<>();

    public Map<String, Plugin> discover() {
        Map<String, Plugin> plugins = new LinkedHashMap<>();
        ServiceLoader.load(Plugin.class).forEach(p -> plugins.put(p.id(), p));

        for (URLClassLoader loader : externalLoaders) {
            ServiceLoader.load(Plugin.class, loader)
                    .forEach(p -> plugins.putIfAbsent(p.id(), p));
        }

        return plugins;
    }

    public void loadExternalJar(Path jarPath) {
        try {
            URL url = jarPath.toUri().toURL();
            URLClassLoader loader = new URLClassLoader(
                    new URL[]{url},
                    PluginRegistry.class.getClassLoader()
            );
            externalLoaders.add(loader);
        } catch (IOException e) {
            throw new RuntimeException(
                    "No se pudo cargar el plugin externo: " + jarPath,
                    e
            );
        }
    }

    public void closeExternalLoaders() {
        for (URLClassLoader loader : externalLoaders) {
            try {
                loader.close();
            } catch (IOException ignored) {
            }
        }
        externalLoaders.clear();
    }
}
