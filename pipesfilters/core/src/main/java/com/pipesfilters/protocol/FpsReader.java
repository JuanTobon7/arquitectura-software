package com.pipesfilters.protocol;


import com.pipesfilters.dtos.FileFrame;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class FpsReader {

    private static final int MAGIC = 0x46505331;

    private final DataInputStream input;

    public FpsReader(InputStream input) {
        this.input = new DataInputStream(input);
    }

    public FileFrame read() throws IOException {

        try {
            int magic = input.readInt();

            if (magic == 0) {
                return null;
            }

            if (magic != MAGIC) {
                throw new IOException("Invalid FPS stream");
            }

            int nameLength = input.readInt();

            byte[] nameBytes = input.readNBytes(nameLength);
            String name = new String(nameBytes, StandardCharsets.UTF_8);

            long contentLength = input.readLong();

            if (contentLength > Integer.MAX_VALUE) {
                throw new IOException("File too large");
            }

            byte[] content = input.readNBytes((int) contentLength);

            if (content.length != contentLength) {
                throw new EOFException("Unexpected end of FPS stream");
            }

            return new FileFrame(name, content);

        } catch (EOFException e) {
            return null;
        }
    }
}