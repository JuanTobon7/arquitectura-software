package com.pipesfilters.extrafilters;

import com.pipesfilters.images.AbstractImagePlugin;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

public class SepiaImagePlugin extends AbstractImagePlugin {

    public SepiaImagePlugin() {
    }

    public SepiaImagePlugin(Path outputDir) {
        super(outputDir);
    }

    @Override
    public String id() {
        return "sepia";
    }

    @Override
    public String suffix() {
        return "sepia";
    }

    @Override
    protected BufferedImage transform(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage sepia = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);

                int a = (rgb >> 24) & 0xFF;
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                int tr = (int) Math.min(255, 0.393 * r + 0.769 * g + 0.189 * b);
                int tg = (int) Math.min(255, 0.349 * r + 0.686 * g + 0.168 * b);
                int tb = (int) Math.min(255, 0.272 * r + 0.534 * g + 0.131 * b);

                int newRgb = (a << 24) | (tr << 16) | (tg << 8) | tb;
                sepia.setRGB(x, y, newRgb);
            }
        }

        return sepia;
    }
}
