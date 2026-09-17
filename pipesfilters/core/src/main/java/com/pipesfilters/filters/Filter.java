package com.pipesfilters.filters;

import com.pipesfilters.protocol.FpsReader;
import com.pipesfilters.protocol.FpsWriter;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public interface Filter {

    void process(
            FpsReader input,
            FpsWriter output
    ) throws  InterruptedException, ExecutionException, IOException;
}