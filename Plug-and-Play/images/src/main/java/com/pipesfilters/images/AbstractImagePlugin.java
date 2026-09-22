package com.pipesfilters.images;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;

import com.pipesfilters.concurrence.ConcurrenceBase;
import com.pipesfilters.core.ProcessingContext;

/**
 * Clase base abstracta para plugins que transforman imágenes.
 * Cada plugin concreto implementa {@link #transform(BufferedImage)}.
 */
public abstract class AbstractImagePlugin extends ConcurrenceBase {

    private Path outputDir;
    private final List<Path> generatedFiles = new ArrayList<>();

    protected AbstractImagePlugin() {
    }

    protected AbstractImagePlugin(Path outputDir) {
        this.outputDir = outputDir;
    }

    @Override
    public void configureOutputDir(Path outputDir) {
        this.outputDir = outputDir;
    }

    public abstract String id();

    public abstract String suffix();

    protected abstract BufferedImage transform(BufferedImage image);

    @Override
    public final List<ProcessingContext> process(List<ProcessingContext> input) {
        List<ProcessingContext> result = super.process(input);
        return result;
    }

    @Override
    protected final ProcessingContext processFile(ProcessingContext ctx) {

        BufferedImage image;

        try (ByteArrayInputStream stream = new ByteArrayInputStream(ctx.payload())) {
            image = ImageIO.read(stream);
        } catch (Exception e) {
            return ctx;
        }

        if (image == null) {
            return ctx;
        }

        BufferedImage transformed = transform(image);
        String baseName = ctx.name();
        int punto = baseName.lastIndexOf('.');
        String nameWithoutExt = punto == -1 ? baseName : baseName.substring(0, punto);
        String suffix = suffix();

        try {
            Files.createDirectories(outputDir);
            List<Path> files = writeVersions(transformed, baseName, nameWithoutExt, suffix);
            generatedFiles.addAll(files);
        } catch (IOException | InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return ctx;
    }

    public Path getOutputDir() {
        return outputDir;
    }

    public List<ProcessingContext> collectOutputContexts() throws IOException {
        List<ProcessingContext> contexts = new ArrayList<>();

        for (Path file : generatedFiles) {
            byte[] content = Files.readAllBytes(file);
            ProcessingContext ctx = new ProcessingContext(
                    file.getFileName().toString(),
                    content
            );
            ctx.putMetadata("ubication", file.toAbsolutePath().toString());
            contexts.add(ctx);
        }

        return contexts;
    }

    private List<Path> writeVersions(
            BufferedImage image,
            String originalName,
            String nameWithoutExt,
            String suffix
    ) throws IOException, InterruptedException, ExecutionException {

        String format = HelperImage.getFormat(originalName);
        List<CompletableFuture<Path>> writes = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            final int index = i;
            writes.add(CompletableFuture.supplyAsync(
                    () -> writeVersion(image, format, nameWithoutExt, suffix, index),
                    sharedExecutor()
            ));
        }

        List<Path> targets = new ArrayList<>();
        for (CompletableFuture<Path> write : writes) {
            targets.add(write.join());
        }

        return targets;
    }

    private Path writeVersion(
            BufferedImage image,
            String format,
            String nameWithoutExt,
            String suffix,
            int index
    ) {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            boolean written = ImageIO.write(image, format, buffer);

            if (!written) {
                throw new IOException(
                        "No se pudo escribir la imagen en formato: " + format
                );
            }

            String outputName = nameWithoutExt + "_" + suffix + "_" + index + "." + format;
            Path target = outputDir.resolve(outputName);
            Files.write(target, buffer.toByteArray());

            return target;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}