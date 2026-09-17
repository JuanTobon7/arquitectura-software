package com.pipefilters.converter;

import com.pipesfilters.concurrence.ConcurrenceBase;
import com.pipesfilters.dtos.FileFrame;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class BinaryToBase64 extends ConcurrenceBase {

    @Override
    protected FileFrame processFile(FileFrame origen) {

        String base64 = Base64.getEncoder()
                .encodeToString(origen.content());

        return new FileFrame(
                origen.name(),
                base64.getBytes(StandardCharsets.UTF_8)
        );
    }
}
