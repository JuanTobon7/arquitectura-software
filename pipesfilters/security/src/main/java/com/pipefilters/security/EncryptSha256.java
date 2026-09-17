package com.pipefilters.security;

import com.pipesfilters.concurrence.ConcurrenceBase;
import com.pipesfilters.dtos.FileFrame;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

public class EncryptSha256 extends ConcurrenceBase
{
    @Override
    protected FileFrame processFile(FileFrame origen) throws Exception {

        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        byte[] hash = digest.digest(origen.content());

        String sha256 = HexFormat.of().formatHex(hash);

        return new FileFrame(
                origen.name(),
                sha256.getBytes(StandardCharsets.UTF_8)
        );
    }
}
