package com.pipesfilters.plugins;

import com.pipesfilters.core.ProcessingContext;

import java.nio.file.Path;
import java.util.List;

public interface Plugin {

    String id();

    /**
     * Sufijo usado en los archivos generados por el plugin.
     * Por defecto devuelve el id; los plugins de imagen pueden sobreescribirlo.
     */
    default String suffix() {
        return id();
    }

    /**
     * Invocado por el cliente u orquestador para establecer el directorio de salida
     * cuando el plugin lo necesita (por ejemplo filtros de imagen).
     */
    default void configureOutputDir(Path outputDir) {
    }

    List<ProcessingContext> process(List<ProcessingContext> input);
}
