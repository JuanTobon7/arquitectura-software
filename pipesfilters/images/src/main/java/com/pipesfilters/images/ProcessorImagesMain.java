package com.pipesfilters.images;

import com.pipesfilters.dtos.FileFrame;
import com.pipesfilters.io.DirectoryIO;
import com.pipesfilters.protocol.FpsReader;
import com.pipesfilters.protocol.FpsWriter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ProcessorImagesMain {

    public static final String DIRECTORY_MARKER = "__DIRECTORY__";

    public static void main(String[] args) {

        try {
            BufferedReader stdin =
                    new BufferedReader(
                            new InputStreamReader(System.in, StandardCharsets.UTF_8)
                    );

            String inputPath = stdin.readLine();

            if (inputPath == null || inputPath.isBlank()) {
                throw new IllegalArgumentException(
                        "Debe proporcionar la ruta de entrada por stdin"
                );
            }

            Path inputDir = Paths.get(inputPath.trim());

            if (!Files.isDirectory(inputDir)) {
                throw new IllegalArgumentException(
                        "La ruta de entrada no es un directorio: "
                                + inputDir
                );
            }

            Path outputBaseDir = DirectoryIO.resolveOutputDir("images");

            ProcessorImages processor =
                    new ProcessorImages(outputBaseDir);

            java.io.ByteArrayOutputStream fpsBuffer =
                    new java.io.ByteArrayOutputStream();
            DirectoryIO.writeAllFrames(fpsBuffer, inputDir);

            FpsReader reader = new FpsReader(
                    new java.io.ByteArrayInputStream(fpsBuffer.toByteArray())
            );
            FpsWriter writer = new FpsWriter(System.out);

            processor.process(reader, writer);

            String dirPath = processor.getLastOutputDir().toAbsolutePath().toString();
            writer.write(new FileFrame(
                    DIRECTORY_MARKER,
                    dirPath.getBytes(StandardCharsets.UTF_8)
            ));
            writer.close();

        } catch (Exception e) {
            System.err.println(
                    "ProcessorImages error: " + e.getMessage()
            );

            System.exit(1);
        }
    }
}