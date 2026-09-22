package com.pipesfilters.images;

import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.nio.file.Path;

public class GrayscaleImagePlugin extends AbstractImagePlugin {

    public GrayscaleImagePlugin() {
    }

    public GrayscaleImagePlugin(Path outputDir) {
        super(outputDir);
    }

    @Override
    public String id() {
        return "grayscale";
    }

    @Override
    public String suffix() {
        return "gray";
    }

    @Override
    protected BufferedImage transform(BufferedImage image) {
        BufferedImage gray = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_INT_ARGB
        );
        Graphics2D g = gray.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();

        ColorConvertOp op = new ColorConvertOp(
                ColorSpace.getInstance(ColorSpace.CS_GRAY),
                null
        );
        return op.filter(gray, null);
    }
}
