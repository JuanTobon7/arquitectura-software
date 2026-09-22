package com.pipesfilters.extrafilters;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.util.Optional;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;

import com.pipesfilters.core.ProcessingContext;
import com.pipesfilters.plugins.Plugin;

/**
 * Plugin que extrae metadata de imágenes y la adjunta al ProcessingContext.
 * No genera archivos de salida; solo metadata.
 */
public class MetadataImagePlugin implements Plugin {

    @Override
    public String id() {
        return "metadata";
    }

    @Override
    public java.util.List<ProcessingContext> process(java.util.List<ProcessingContext> input) {
        for (ProcessingContext ctx : input) {
            ctx.putMetadata("appliedPlugin", id());
            extractMetadata(ctx);
        }
        return input;
    }

    private void extractMetadata(ProcessingContext ctx) {
        try (ByteArrayInputStream stream = new ByteArrayInputStream(ctx.payload());
             ImageInputStream iis = ImageIO.createImageInputStream(stream)) {

            java.util.Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (!readers.hasNext()) {
                return;
            }

            ImageReader reader = readers.next();
            reader.setInput(iis, true);
            BufferedImage image = reader.read(0);
            IIOMetadata metadata = reader.getImageMetadata(0);

            ctx.putMetadata("width", image.getWidth());
            ctx.putMetadata("height", image.getHeight());
            ctx.putMetadata("format", reader.getFormatName());

            if (metadata != null && metadata.getAsTree(metadata.getNativeMetadataFormatName()) != null) {
                ctx.putMetadata("hasNativeMetadata", true);
            }

            reader.dispose();

        } catch (Exception e) {
            ctx.putMetadata("metadataError", e.getMessage());
        }
    }
}
