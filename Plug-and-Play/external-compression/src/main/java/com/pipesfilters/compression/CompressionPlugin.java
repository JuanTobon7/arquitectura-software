package com.pipesfilters.compression;

import com.pipesfilters.core.ProcessingContext;
import com.pipesfilters.plugins.Plugin;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.zip.Deflater;

public class CompressionPlugin implements Plugin {

    @Override
    public String id() {
        return "compression";
    }

    @Override
    public List<ProcessingContext> process(List<ProcessingContext> input) {
        for (ProcessingContext ctx : input) {
            byte[] compressed = compress(ctx.payload());
            ctx.putMetadata("compressedSize", (long) compressed.length);
            ctx.putMetadata("compressionRatio", (double) compressed.length / ctx.payload().length);
        }
        return input;
    }

    private byte[] compress(byte[] data) {
        Deflater deflater = new Deflater();
        deflater.setInput(data);
        deflater.finish();

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length)) {
            byte[] buffer = new byte[1024];
            while (!deflater.finished()) {
                int count = deflater.deflate(buffer);
                outputStream.write(buffer, 0, count);
            }
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            deflater.end();
        }
    }
}
