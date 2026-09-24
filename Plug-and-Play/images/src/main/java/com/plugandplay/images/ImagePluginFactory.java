package com.plugandplay.images;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Fábrica que permite registrar y construir plugins de imagen por nombre.
 * Facilita que el gestor de componentes cree instancias pasando el outputDir.
 */
public final class ImagePluginFactory {

    private ImagePluginFactory() {
    }

    public static Map<String, Function<Path, AbstractImagePlugin>> supportedPlugins() {
        return Map.of(
                "grayscale", GrayscaleImagePlugin::new
        );
    }

    public static List<String> fixedPluginIds() {
        return List.of("grayscale");
    }
}
