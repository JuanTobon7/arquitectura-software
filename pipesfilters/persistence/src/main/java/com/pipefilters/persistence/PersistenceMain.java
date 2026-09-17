package com.pipefilters.persistence;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class PersistenceMain {

    public static void main(String[] args) {

        try {
            BufferedReader stdin =
                    new BufferedReader(
                            new InputStreamReader(System.in)
                    );

            String directoryPath = stdin.readLine();

            if (directoryPath == null || directoryPath.isBlank()) {
                throw new IllegalArgumentException(
                        "Debe proporcionar la ruta del directorio por stdin"
                );
            }

            Path directory = Paths.get(directoryPath);

            if (!Files.isDirectory(directory)) {
                throw new IllegalArgumentException(
                        "La ruta no es un directorio: " + directory
                );
            }

            StringBuilder listing = new StringBuilder();

            try (Stream<Path> paths = Files.list(directory)) {
                paths
                        .filter(Files::isRegularFile)
                        .sorted()
                        .forEach(p -> listing
                                .append(p.getFileName().toString())
                                .append(System.lineSeparator())
                        );
            }

            System.out.print(listing);

        } catch (Exception e) {
            System.err.println("Persistence error: " + e.getMessage());
            System.exit(1);
        }
    }
}
