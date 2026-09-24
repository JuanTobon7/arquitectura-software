package com.plugandplay.converter;

import com.plugandplay.core.concurrence.ConcurrenceBase;
import com.plugandplay.core.ProcessingContext;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Base64ToBinary extends ConcurrenceBase {

    @Override
    public String id() {
        return "base64-to-binary";
    }

    @Override
    protected ProcessingContext processFile(ProcessingContext ctx) {
        try {
            String base64 = new String(
                    ctx.payload(),
                    StandardCharsets.UTF_8
            );

            byte[] binary = Base64.getDecoder().decode(base64);
            ctx.setPayload(binary);
        } catch (IllegalArgumentException e) {
            // Payload no es base64 valido; se deja sin cambios.
        }
        return ctx;
    }
}
