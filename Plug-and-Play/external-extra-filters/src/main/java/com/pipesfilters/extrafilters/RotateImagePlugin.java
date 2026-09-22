package com.pipesfilters.extrafilters;

import com.pipesfilters.images.AbstractImagePlugin;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.nio.file.Path;

public class RotateImagePlugin extends AbstractImagePlugin {

    public RotateImagePlugin() {
    }

    public RotateImagePlugin(Path outputDir) {
        super(outputDir);
    }

    @Override
    public String id() {
        return "rotate";
    }

    @Override
    public String suffix() {
        return "rot";
    }

    @Override
    protected BufferedImage transform(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();

        BufferedImage rotated = new BufferedImage(h, w, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = rotated.createGraphics();

        AffineTransform transform = new AffineTransform();
        transform.translate((h - w) / 2.0, (h - w) / 2.0);
        transform.rotate(Math.toRadians(90), w / 2.0, h / 2.0);

        g.drawImage(image, transform, null);
        g.dispose();

        return rotated;
    }
}
