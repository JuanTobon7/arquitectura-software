package com.pipefilters.commands;

import com.pipefilters.commands.components.CommandLineInputNormalizer;
import com.pipefilters.commands.components.CommandPipelineRunner;
import com.pipefilters.commands.components.FilesSearcherComponent;
import com.pipefilters.commands.components.ParsedCommand;
import com.pipefilters.commands.components.PipelineReporter;

import java.nio.file.Path;
import java.util.List;

public class CommandsOrchestrator {

    private final CommandsStrategy strategy;
    private final CommandPipelineRunner pipelineRunner;

    public CommandsOrchestrator(
            CommandsStrategy strategy
    ) {
        this.strategy = strategy;
        this.pipelineRunner = new CommandPipelineRunner(strategy);
    }

    public void execute(
            String commandLine
    ) throws Exception {

        ParsedCommand parsed =
                CommandLineInputNormalizer.parse(commandLine);

        String input =
                CommandLineInputNormalizer.normalizeInput(parsed.input());

        String extension =
                CommandLineInputNormalizer.getImageExtension(parsed.commands());

        List<Path> files =
                FilesSearcherComponent.search(
                        input,
                        extension
                );

        if (files.isEmpty()) {
            PipelineReporter.printNoFilesFound(extension);
            return;
        }

        List<CommandsList> commands =
                parsed.commands()
                        .stream()
                        .map(Command::type)
                        .toList();

        strategy.buildPipeline(commands);

        PipelineReporter.printPipeline(
                input,
                files,
                parsed.commands()
        );

        for (Path file : files) {
            System.out.println();
            System.out.println("Processing: " + file);
            pipelineRunner.executeFile(file, parsed.commands());
        }

        System.out.println();
        System.out.println("Pipeline completed.");
    }
}