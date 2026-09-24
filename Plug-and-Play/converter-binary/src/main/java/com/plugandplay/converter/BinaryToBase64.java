package com.plugandplay.converter;

import com.plugandplay.core.concurrence.ConcurrenceBase;
import com.plugandplay.core.ProcessingContext;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class BinaryToBase64 extends ConcurrenceBase {

    @Override
    public String id() {
        return "binary-to-base64";
    }

    @Override
    protected ProcessingContext processFile(ProcessingContext ctx) {

        String base64 = Base64.getEncoder()
                .encodeToString(ctx.payload());

        ctx.setPayload(base64.getBytes(StandardCharsets.UTF_8));
        return ctx;
    }
}
