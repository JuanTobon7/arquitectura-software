package com.pipefilters.converter;

import com.pipesfilters.protocol.FpsReader;
import com.pipesfilters.protocol.FpsWriter;

public class Base64ToBinaryMain {

    public static void main(String[] args) {

        try {
            Base64ToBinary filter = new Base64ToBinary();

            FpsReader reader = new FpsReader(System.in);
            FpsWriter writer = new FpsWriter(System.out);

            filter.process(reader, writer);

        } catch (Exception e) {
            System.err.println("Base64ToBinary error: " + e.getMessage());
            System.exit(1);
        }
    }
}
