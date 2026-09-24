package com.plugandplay.persistence;

import com.plugandplay.core.concurrence.ConcurrenceBase;
import com.plugandplay.core.ProcessingContext;
import com.plugandplay.core.plugins.TerminalPlugin;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.HexFormat;

public class Persistence extends ConcurrenceBase implements TerminalPlugin {

    private java.nio.file.Path outputDir;

    private final Repository repository;

    public Persistence() {
        this.repository = Factory.create();
    }

    @Override
    public void configureOutputDir(java.nio.file.Path outputDir) {
        this.outputDir = outputDir;
    }

    @Override
    public String id() {
        return "persistence";
    }

    @Override
    protected ProcessingContext processFile(ProcessingContext ctx) {

        String hash = ctx.getMetadata("sha256", String.class)
                .orElseGet(() -> sha256Hex(ctx.payload()));

        String ubication = ctx.getMetadata("ubication", String.class)
                .orElse("")
                .toString();

        ProcessedFile record = new ProcessedFile(
                ctx.name(),
                hash,
                ubication,
                ctx.originalSize(),
                id(),
                LocalDateTime.now()
        );

        repository.save(record);
        writeResultTxt(ctx);
        return ctx;
    }

    private void writeResultTxt(ProcessingContext ctx) {
        if (outputDir == null) {
            return;
        }
        try {
            java.nio.file.Files.createDirectories(outputDir);
            java.nio.file.Path target = outputDir.resolve("resultado.txt");
            String hash = ctx.getMetadata("sha256", String.class).orElseGet(() -> sha256Hex(ctx.payload()));
            String ubication = ctx.getMetadata("ubication", String.class).orElse("");
            String plugins = ctx.getMetadata("appliedPlugins", String.class).orElse(id());
            String width = ctx.getMetadata("width", Integer.class).map(String::valueOf).orElse("N/A");
            String height = ctx.getMetadata("height", Integer.class).map(String::valueOf).orElse("N/A");
            String format = ctx.getMetadata("format", String.class).orElse("N/A");
            String metadataError = ctx.getMetadata("metadataError", String.class).orElse("");
            StringBuilder sb = new StringBuilder();
            sb.append("Archivo: ").append(ctx.name()).append(System.lineSeparator());
            sb.append("SHA256: ").append(hash).append(System.lineSeparator());
            sb.append("Tamanio original: ").append(ctx.originalSize()).append(System.lineSeparator());
            sb.append("Ubicacion: ").append(ubication).append(System.lineSeparator());
            sb.append("Plugins aplicados: ").append(plugins).append(System.lineSeparator());
            sb.append("Formato: ").append(format).append(System.lineSeparator());
            sb.append("Dimensiones: ").append(width).append("x").append(height).append(System.lineSeparator());
            if (!metadataError.isBlank()) {
                sb.append("Error metadata: ").append(metadataError).append(System.lineSeparator());
            }
            sb.append("Fecha procesamiento: ").append(LocalDateTime.now()).append(System.lineSeparator());
            java.nio.file.Files.writeString(target, sb.toString(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String sha256Hex(byte[] payload) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(payload);
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
