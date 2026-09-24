package com.plugandplay.security;

import com.plugandplay.core.concurrence.ConcurrenceBase;
import com.plugandplay.core.ProcessingContext;

import java.security.MessageDigest;
import java.util.HexFormat;

public class EncryptSha256 extends ConcurrenceBase {

    @Override
    public String id() {
        return "security";
    }

    @Override
    protected ProcessingContext processFile(ProcessingContext ctx) {

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(ctx.payload());
            String sha256 = HexFormat.of().formatHex(hash);
            ctx.putMetadata("sha256", sha256);
            return ctx;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
