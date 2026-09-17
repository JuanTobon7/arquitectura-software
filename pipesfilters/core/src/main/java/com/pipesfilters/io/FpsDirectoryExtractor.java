package com.pipesfilters.io;

import com.pipesfilters.dtos.FileFrame;
import com.pipesfilters.protocol.FpsReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class FpsDirectoryExtractor {

    public static final String DIRECTORY_MARKER = "__DIRECTORY__";

    private FpsDirectoryExtractor() {
    }

    public static String extractDirectoryPath(InputStream input) throws IOException {
        FpsReader reader = new FpsReader(input);
        FileFrame frame;
        FileFrame last = null;

        while ((frame = reader.read()) != null) {
            last = frame;
        }

        if (last == null) {
            throw new IllegalArgumentException("No se recibio ningun frame FPS");
        }

        if (!DIRECTORY_MARKER.equals(last.name())) {
            throw new IllegalArgumentException(
                    "El ultimo frame no es un marcador de directorio: " + last.name()
            );
        }

        return new String(last.content(), StandardCharsets.UTF_8);
    }
}
