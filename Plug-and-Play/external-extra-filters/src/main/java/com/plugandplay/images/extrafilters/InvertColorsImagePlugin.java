package com.plugandplay.images.extrafilters;

import com.plugandplay.images.AbstractImagePlugin;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

public class InvertColorsImagePlugin extends AbstractImagePlugin {

    public InvertColorsImagePlugin() {
    }

    public InvertColorsImagePlugin(Path outputDir) {
        super(outputDir);
    }

    @Override
    public String id() {
        return "invert";
    }

    @Override
    public String suffix() {
        return "inv";
    }

    @Override
    protected BufferedImage transform(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage inverted = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int a = (rgb >> 24) & 0xFF;
                int r = 255 - ((rgb >> 16) & 0xFF);
                int g = 255 - ((rgb >> 8) & 0xFF);
                int b = 255 - (rgb & 0xFF);
                inverted.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }

        return inverted;
    }
}
