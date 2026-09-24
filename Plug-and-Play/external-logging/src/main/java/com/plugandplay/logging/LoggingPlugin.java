package com.plugandplay.logging;

import com.plugandplay.core.ProcessingContext;
import com.plugandplay.core.plugins.Plugin;

import java.time.Instant;
import java.util.List;

public class LoggingPlugin implements Plugin {

    @Override
    public String id() {
        return "logging";
    }

    @Override
    public List<ProcessingContext> process(List<ProcessingContext> input) {
        System.out.println("[" + Instant.now() + "] Plugin logging - archivos recibidos:");
        for (ProcessingContext ctx : input) {
            System.out.println("  - " + ctx.name() + " (" + ctx.originalSize() + " bytes)");
        }
        return input;
    }
}
