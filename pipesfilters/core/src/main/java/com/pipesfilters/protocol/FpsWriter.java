package com.pipesfilters.protocol;

import com.pipesfilters.dtos.FileFrame;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class FpsWriter {

    private static final int MAGIC = 0x46505331; // "FPS1"

    private final DataOutputStream output;

    public FpsWriter(OutputStream output) {
        this.output = new DataOutputStream(output);
    }

    public void write(FileFrame frame) throws IOException {
        byte[] name = frame.name().getBytes(StandardCharsets.UTF_8);
        byte[] content = frame.content();

        output.writeInt(MAGIC);
        output.writeInt(name.length);
        output.write(name);

        output.writeLong(content.length);
        output.write(content);
    }

    public void close() throws IOException {
        output.writeInt(0);
        output.flush();
        output.close();
    }
}