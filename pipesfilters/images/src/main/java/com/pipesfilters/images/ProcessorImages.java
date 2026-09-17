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
    protected FileFrame processFile(FileFrame origen) throws Exception {

        BufferedImage imagen = ImageIO.read(
                new ByteArrayInputStream(origen.content())
        );

        if (imagen == null) {
            throw new IOException(
                    "El archivo no es una imagen válida: " + origen.name()
            );
        }


        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        String format = HelperImage.getFormat(origen.name());

        boolean written = ImageIO.write(
                imagen,
                format,
                buffer
        );

        if (!written) {
            throw new IOException(
                    "No se pudo escribir la imagen en formato: " + format
            );
        }

        return new FileFrame(
                origen.name(),
                buffer.toByteArray()
        );
    }
}
