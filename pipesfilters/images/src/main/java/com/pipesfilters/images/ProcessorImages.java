package com.pipesfilters.images;

import com.pipesfilters.concurrence.ConcurrenceBase;
import com.pipesfilters.dtos.FileFrame;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ProcessorImages extends ConcurrenceBase {

    @Override
    public void process(com.pipesfilters.protocol.FpsReader entrada, com.pipesfilters.protocol.FpsWriter salida) throws InterruptedException, java.util.concurrent.ExecutionException, java.io.IOException {

        java.nio.file.Path outputDir = java.nio.file.Paths.get("images-output-" + System.currentTimeMillis());
        java.nio.file.Files.createDirectories(outputDir);

        java.util.List<String> writtenFiles = new java.util.ArrayList<>();

        com.pipesfilters.dtos.FileFrame frame;
        while ((frame = entrada.read()) != null) {
            // validar que es imagen
            BufferedImage imagen = null;
            try {
                imagen = ImageIO.read(new ByteArrayInputStream(frame.content()));
            } catch (Exception ignored) {}

            if (imagen == null) {
                // no es imagen: saltar
                continue;
            }

            String format = HelperImage.getFormat(frame.name());
            String baseName = frame.name();
            int punto = baseName.lastIndexOf('.');
            String nameWithoutExt = (punto == -1) ? baseName : baseName.substring(0, punto);

            // crear copias con pool de 5 hilos SOLO para este filtro
            final BufferedImage imgCopy = imagen;
            final String formatCopy = format;
            final String nameCopy = nameWithoutExt;

            java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(5);
            java.util.List<java.util.concurrent.Future<?>> tasks = new java.util.ArrayList<>();

            for (int i = 1; i <= 5; i++) {
                final int idx = i;
                tasks.add(pool.submit(() -> {
                    try {
                        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
                        boolean written = ImageIO.write(imgCopy, formatCopy, buffer);
                        if (!written) throw new java.io.IOException("No se pudo escribir la imagen en formato: " + formatCopy);

                        String outName = nameCopy + "_" + idx + "." + formatCopy;
                        java.nio.file.Path target = outputDir.resolve(outName);
                        java.nio.file.Files.write(target, buffer.toByteArray());

                        synchronized (writtenFiles) {
                            writtenFiles.add(target.toAbsolutePath().toString());
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }));
            }

            // esperar a que terminen las 5 tareas para esta imagen
            for (java.util.concurrent.Future<?> t : tasks) {
                try { t.get(); } catch (Exception e) { throw new RuntimeException(e); }
            }
            pool.shutdown();
        }

        // preparar contenido con el path del directorio (como bytes UTF-8)
        String dirPath = outputDir.toAbsolutePath().toString();
        com.pipesfilters.dtos.FileFrame outFrame = new com.pipesfilters.dtos.FileFrame(dirPath, dirPath.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        salida.write(outFrame);
        salida.close();
    }

    @Override
    protected FileFrame processFile(FileFrame origen) throws Exception {
        throw new UnsupportedOperationException("ProcessorImages utiliza su propia implementación de process().");
    }
}
