package com.pipefilters.converter;

import com.pipesfilters.concurrence.ConcurrenceBase;
import com.pipesfilters.dtos.FileFrame;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Base64ToBinary extends ConcurrenceBase {

    @Override
    protected FileFrame processFile(FileFrame origen) {

        String base64 = new String(
                origen.content(),
                StandardCharsets.UTF_8
        );

        byte[] binary = Base64.getDecoder()
                .decode(base64);

        return new FileFrame(
                origen.name(),
                binary
        );
    }
}
