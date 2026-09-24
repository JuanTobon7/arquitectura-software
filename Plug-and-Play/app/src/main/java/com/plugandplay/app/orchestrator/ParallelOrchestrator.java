package com.plugandplay.app.orchestrator;

import com.plugandplay.core.ProcessingContext;
import com.plugandplay.core.plugins.Plugin;
import com.plugandplay.core.plugins.TerminalPlugin;
import com.plugandplay.core.registry.ComponentManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ParallelOrchestrator {

    private final ComponentManager componentManager;

    public ParallelOrchestrator() {
        this.componentManager = new ComponentManager();
    }

    public ParallelOrchestrator(ComponentManager componentManager) {
        this.componentManager = componentManager;
    }

    public List<ProcessingContext> run(List<ProcessingContext> input, java.nio.file.Path outputDir) {
        return run(input, outputDir, List.of());
    }

    public List<ProcessingContext> run(List<ProcessingContext> input, java.nio.file.Path outputDir, List<String> selectedIds) {
        Map<String, Plugin> discovered = componentManager.discoverPlugins();

        for (Plugin plugin : discovered.values()) {
            plugin.configureOutputDir(outputDir);
        }

        List<Plugin> processingPlugins = discovered.values().stream()
                .filter(p -> !(p instanceof TerminalPlugin))
                .filter(p -> selectedIds.isEmpty() || selectedIds.contains(p.id()))
                .collect(Collectors.toList());

        TerminalPlugin savePlugin = discovered.values().stream()
                .filter(p -> p instanceof TerminalPlugin)
                .map(p -> (TerminalPlugin) p)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No hay plugin terminal (save) registrado"
                ));

        List<String> appliedPluginIds = new ArrayList<>();
        List<CompletableFuture<List<ProcessingContext>>> branches = processingPlugins.stream()
                .map(plugin -> CompletableFuture.supplyAsync(
                        () -> plugin.process(copyAll(input)),
                        CompletableFuture.supplyAsync(() -> null).defaultExecutor()
                ))
                .collect(Collectors.toList());

        CompletableFuture<Void> allDone =
                CompletableFuture.allOf(branches.toArray(new CompletableFuture[0]));

        return allDone.thenApply(v -> {
            List<ProcessingContext> merged = mergeBranches(input, branches);
            return savePlugin.process(merged);
        }).join();
    }

    public List<ProcessingContext> run(List<ProcessingContext> input) {
        return run(input, java.nio.file.Path.of("result"));
    }

    private List<ProcessingContext> copyAll(List<ProcessingContext> input) {
        return input.stream().map(ProcessingContext::copy).collect(Collectors.toList());
    }

    private List<ProcessingContext> mergeBranches(
            List<ProcessingContext> original,
            List<CompletableFuture<List<ProcessingContext>>> branches
    ) {
        Map<String, ProcessingContext> byName = new LinkedHashMap<>();
        original.forEach(ctx -> byName.put(ctx.name(), ctx.copy()));

        for (CompletableFuture<List<ProcessingContext>> branch : branches) {
            for (ProcessingContext result : branch.join()) {
                ProcessingContext target = byName.get(result.name());
                if (target != null) {
                    target.mergeMetadataFrom(result);
                }
            }
        }

        return new ArrayList<>(byName.values());
    }
}



