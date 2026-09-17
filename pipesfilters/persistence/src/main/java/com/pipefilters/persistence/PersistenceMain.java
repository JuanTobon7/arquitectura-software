package com.pipefilters.persistence;

import com.pipesfilters.io.FpsDirectoryExtractor;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.stream.Stream;

public class PersistenceMain {

    public static void main(String[] args) {

        try {
            byte[] allInput = System.in.readAllBytes();

            String directoryPath;
            try {
                directoryPath = FpsDirectoryExtractor.extractDirectoryPath(
                        new ByteArrayInputStream(allInput)
                );
            } catch (IllegalArgumentException e) {
                String line = new java.io.BufferedReader(
                        new java.io.InputStreamReader(
                                new ByteArrayInputStream(allInput),
                                StandardCharsets.UTF_8
                        )
                ).readLine();
                if (line == null || line.isBlank()) {
                    throw new IllegalArgumentException(
                            "Debe proporcionar la ruta del directorio por stdin"
                    );
                }
                directoryPath = line.trim();
            }

            Path directory = Path.of(directoryPath);

            if (!Files.isDirectory(directory)) {
                throw new IllegalArgumentException(
                        "La ruta no es un directorio: " + directory
                );
            }

            Repository repository = Factory.create();

            StringBuilder listing = new StringBuilder();

            try (Stream<Path> paths = Files.list(directory)) {
                paths
                        .filter(Files::isRegularFile)
                        .sorted()
                        .forEach(p -> {
                            try {
                                byte[] content = Files.readAllBytes(p);
                                String sha = sha256(content);

                                ProcessedFile file = new ProcessedFile(
                                        p.getFileName().toString(),
                                        sha,
                                        content.length,
                                        "save",
                                        LocalDateTime.now()
                                );
                                repository.save(file);

                                listing
                                        .append(p.getFileName().toString())
                                        .append(System.lineSeparator());
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });
            }

            System.out.print(listing);

        } catch (Exception e) {
            System.err.println("Persistence error: " + e.getMessage());
            System.exit(1);
        }
    }

    private static String sha256(byte[] content) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(content);
        return HexFormat.of().formatHex(hash);
    }
}
