package com.pipefilters.persistence;

import com.pipesfilters.concurrence.ConcurrenceBase;
import com.pipesfilters.dtos.FileFrame;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;

public class Persistence extends ConcurrenceBase {

    private final Repository repository;

    public Persistence() {
        this.repository = Factory.create();
    }

    @Override
    protected FileFrame processFile(FileFrame origen) throws Exception {

        byte[] content = origen.content();
        String sha256 = sha256(content);

        ProcessedFile file = new ProcessedFile(
                origen.name(),
                sha256,
                origen.size(),
                "save",
                LocalDateTime.now()
        );

        repository.save(file);

        return origen;
    }

    private String sha256(byte[] content) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(content);
        return HexFormat.of().formatHex(hash);
    }
}