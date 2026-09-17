package com.pipesfilters.images;

import com.pipesfilters.protocol.FpsReader;
import com.pipesfilters.protocol.FpsWriter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class ProcessorImagesMain {

    public static void main(String[] args) {

        try {
            BufferedReader stdin =
                    new BufferedReader(
                            new InputStreamReader(System.in)
                    );

            String inputPath = stdin.readLine();
            String outputPath = stdin.readLine();

            if (inputPath == null || inputPath.isBlank()) {
                throw new IllegalArgumentException(
                        "Debe proporcionar la ruta de entrada por stdin"
                );
            }

            Path inputDir = Paths.get(inputPath);

            Path outputDir;

            if (outputPath == null || outputPath.isBlank()) {
                outputDir = Paths.get(".").toAbsolutePath().normalize();
            } else {
                outputDir = Paths.get(outputPath);
            }

            if (!Files.isDirectory(inputDir)) {
                throw new IllegalArgumentException(
                        "La ruta de entrada no es un directorio: "
                                + inputDir
                );
            }

            ProcessorImages processor =
                    new ProcessorImages(outputDir);

            FpsReader reader = new FpsReader(
                    new DirectoryImageStream(inputDir)
            );
            FpsWriter writer = new FpsWriter(System.out);

            processor.process(reader, writer);

            String dirPath = findOutputDirectory(outputDir);

            System.out.println(dirPath);

        } catch (Exception e) {
            System.err.println(
                    "ProcessorImages error: " + e.getMessage()
            );

            System.exit(1);
        }
    }

    private static String findOutputDirectory(Path outputDir) throws Exception {
        try (Stream<Path> paths = Files.list(outputDir)) {
            return paths
                    .filter(Files::isDirectory)
                    .filter(p -> p.getFileName().toString().startsWith("images-output-"))
                    .max(java.util.Comparator.comparing(p -> p.getFileName().toString()))
                    .map(p -> p.toAbsolutePath().toString())
                    .orElse(outputDir.toAbsolutePath().toString());
        }
    }

    private static final class DirectoryImageStream extends java.io.InputStream {

        private final Path directory;
        private java.io.InputStream current;
        private java.util.Iterator<Path> iterator;
        private boolean finished = false;

        DirectoryImageStream(Path directory) {
            this.directory = directory;
        }

        @Override
        public int read() throws java.io.IOException {
            ensureOpen();
            while (true) {
                if (current != null) {
                    int value = current.read();
                    if (value != -1) {
                        return value;
                    }
                    current.close();
                    current = null;
                }
                if (!iterator.hasNext()) {
                    finished = true;
                    return -1;
                }
                Path next = iterator.next();
                if (Files.isRegularFile(next)) {
                    current = java.nio.file.Files.newInputStream(next);
                }
            }
        }

        private void ensureOpen() throws java.io.IOException {
            if (finished) {
                return;
            }
            if (iterator == null) {
                this.iterator = Files.list(directory).iterator();
            }
        }
    }
}