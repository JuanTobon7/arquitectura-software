package com.pipefilters.persistence;

import com.pipesfilters.concurrence.ConcurrenceBase;
import com.pipesfilters.dtos.FileFrame;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;

public class Persistence extends ConcurrenceBase {

    private final Repository repository;

    public Persistence() {
        this.repository = Factory.create();
    }

    @Override
    protected FileFrame processFile(FileFrame origen) throws Exception {

        // Si el contenido representa un path a un directorio, generar un archivo de texto
        // con el listado de archivos procesados en ese directorio.
        try {
            String contentAsString = new String(origen.content(), java.nio.charset.StandardCharsets.UTF_8);
            java.nio.file.Path path = java.nio.file.Paths.get(contentAsString);
            if (java.nio.file.Files.exists(path) && java.nio.file.Files.isDirectory(path)) {
                StringBuilder sb = new StringBuilder();
                try (java.util.stream.Stream<java.nio.file.Path> stream = java.nio.file.Files.list(path)) {
                    stream.forEach(p -> sb.append(p.getFileName().toString()).append(System.lineSeparator()));
                }
                String listing = sb.toString();
                // También registrar en la base de datos cada archivo listado
                try (java.util.stream.Stream<java.nio.file.Path> stream = java.nio.file.Files.list(path)) {
                    stream.forEach(p -> {
                        try {
                            byte[] fileBytes = java.nio.file.Files.readAllBytes(p);
                            String sha = sha256(fileBytes);
                            ProcessedFile file = new ProcessedFile(
                                    p.getFileName().toString(),
                                    sha,
                                    fileBytes.length,
                                    "save",
                                    LocalDateTime.now()
                            );
                            repository.save(file);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                }

                return new FileFrame("processed_list.txt", listing.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }
        } catch (Exception ignored) {
            // no es directorio -> continuar con comportamiento anterior
        }

        // comportamiento previo: persistir un solo archivo
        byte[] content = origen.content();
        String sha256 = sha256(content);

        ProcessedFile file = new ProcessedFile(
                origen.name(),
                sha256,
                origen.size(),
                "save",
                LocalDateTime.now()
        );

        repository.save(file);

        return origen;
    }

    private String sha256(byte[] content) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(content);
        return HexFormat.of().formatHex(hash);
    }
}