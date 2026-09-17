package com.pipefilters.converter;

import com.pipesfilters.protocol.FpsReader;
import com.pipesfilters.protocol.FpsWriter;

public class BinaryToBase64Main {

    public static void main(String[] args) {

        try {
            BinaryToBase64 filter = new BinaryToBase64();

            FpsReader reader = new FpsReader(System.in);
            FpsWriter writer = new FpsWriter(System.out);

            filter.process(reader, writer);

        } catch (Exception e) {
            System.err.println("BinaryToBase64 error: " + e.getMessage());
            System.exit(1);
        }
    }
}
