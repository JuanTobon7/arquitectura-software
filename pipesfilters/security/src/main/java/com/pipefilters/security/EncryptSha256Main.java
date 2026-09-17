package com.pipefilters.security;

import com.pipesfilters.protocol.FpsReader;
import com.pipesfilters.protocol.FpsWriter;

public class EncryptSha256Main {

    public static void main(String[] args) {

        try {
            EncryptSha256 filter = new EncryptSha256();

            FpsReader reader = new FpsReader(System.in);
            FpsWriter writer = new FpsWriter(System.out);

            filter.process(reader, writer);

        } catch (Exception e) {
            System.err.println("EncryptSha256 error: " + e.getMessage());
            System.exit(1);
        }
    }
}
