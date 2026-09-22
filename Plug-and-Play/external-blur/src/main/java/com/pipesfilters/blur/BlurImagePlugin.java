package com.pipesfilters.blur;

import com.pipesfilters.images.AbstractImagePlugin;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.nio.file.Path;

public class BlurImagePlugin extends AbstractImagePlugin {

    public BlurImagePlugin() {
    }

    public BlurImagePlugin(Path outputDir) {
        super(outputDir);
    }

    @Override
    public String id() {
        return "blur";
    }

    @Override
    public String suffix() {
        return "blur";
    }

    @Override
    protected BufferedImage transform(BufferedImage image) {
        float[] matrix = {
                1f / 16f, 2f / 16f, 1f / 16f,
                2f / 16f, 4f / 16f, 2f / 16f,
                1f / 16f, 2f / 16f, 1f / 16f
        };

        ConvolveOp op = new ConvolveOp(new Kernel(3, 3, matrix), ConvolveOp.EDGE_NO_OP, null);

        BufferedImage blurred = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_INT_ARGB
        );
        Graphics2D g = blurred.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();

        return op.filter(blurred, null);
    }
}
