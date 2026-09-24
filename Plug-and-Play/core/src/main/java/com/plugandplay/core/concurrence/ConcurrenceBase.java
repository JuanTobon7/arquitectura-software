package com.plugandplay.core.concurrence;

import com.plugandplay.core.ProcessingContext;
import com.plugandplay.core.plugins.Plugin;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public abstract class ConcurrenceBase implements Plugin {

    protected final ExecutorService executor =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    protected ExecutorService sharedExecutor() {
        return executor;
    }

    protected abstract ProcessingContext processFile(ProcessingContext ctx);

    @Override
    public List<ProcessingContext> process(List<ProcessingContext> input) {
        List<CompletableFuture<ProcessingContext>> futures = input.stream()
                .map(ctx -> CompletableFuture.supplyAsync(() -> processFile(ctx), executor))
                .collect(Collectors.toList());

        List<ProcessingContext> results = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        for (ProcessingContext ctx : results) {
            ctx.putMetadata("appliedPlugin", id());
        }
        return results;
    }
}
