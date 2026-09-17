package com.pipesfilters.images;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;

import com.pipesfilters.concurrence.ConcurrenceBase;
import com.pipesfilters.dtos.FileFrame;
import com.pipesfilters.protocol.FpsReader;
import com.pipesfilters.protocol.FpsWriter;

public class ProcessorImages extends ConcurrenceBase {

    private final Path outputBaseDir;

    public ProcessorImages(Path outputBaseDir) {
        this.outputBaseDir = outputBaseDir;
    }

    @Override
    public void process(
            FpsReader entrada,
            FpsWriter salida
    ) throws InterruptedException, ExecutionException, IOException {

        Path outputDir = outputBaseDir.resolve(
                "images-output-" + System.currentTimeMillis()
        );

        Files.createDirectories(outputDir);

        FileFrame frame;

        while ((frame = entrada.read()) != null) {

            BufferedImage imagen;

            try {
                imagen = ImageIO.read(
                        new ByteArrayInputStream(frame.content())
                );
            } catch (Exception e) {
                continue;
            }

            if (imagen == null) {
                continue;
            }

            processImage(imagen, frame.name(), outputDir);
        }

        String dirPath = outputDir.toAbsolutePath().toString();

        salida.write(
                new FileFrame(
                        dirPath,
                        dirPath.getBytes(StandardCharsets.UTF_8)
                )
        );

        salida.close();
    }

    private void processImage(
            BufferedImage image,
            String originalName,
            Path outputDir
    ) throws IOException, InterruptedException {

        String format = HelperImage.getFormat(originalName);

        String baseName = originalName;
        int punto = baseName.lastIndexOf('.');

        String nameWithoutExt =
                punto == -1
                        ? baseName
                        : baseName.substring(0, punto);

        ExecutorService pool = Executors.newFixedThreadPool(5);

        try {
            List<Future<?>> tasks = new ArrayList<>();

            for (int i = 1; i <= 5; i++) {
                final int index = i;

                tasks.add(pool.submit(() -> {
                    try {
                        ByteArrayOutputStream buffer =
                                new ByteArrayOutputStream();

                        boolean written =
                                ImageIO.write(image, format, buffer);

                        if (!written) {
                            throw new IOException(
                                    "No se pudo escribir la imagen en formato: "
                                            + format
                            );
                        }

                        String outputName =
                                nameWithoutExt
                                        + "_"
                                        + index
                                        + "."
                                        + format;

                        Path target = outputDir.resolve(outputName);

                        Files.write(
                                target,
                                buffer.toByteArray()
                        );

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }));
            }

            for (Future<?> task : tasks) {
                task.get();
            }

        } catch (java.util.concurrent.ExecutionException e) {
            throw new RuntimeException(e.getCause());
        } finally {
            pool.shutdown();
        }
    }

    @Override
    protected FileFrame processFile(FileFrame origen) throws Exception {
        throw new UnsupportedOperationException(
                "ProcessorImages utiliza su propia implementación de process()."
        );
    }
}