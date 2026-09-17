package com.pipefilters.commands.components;

import com.pipefilters.commands.Command;
import com.pipefilters.commands.CommandsList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ParserPipeline {
    public static List<Command> parse(
            String pipeline
    ) {

        String[] rawCommands =
                pipeline.split("\\|");

        List<Command> commands =
                new ArrayList<>();

        for (String rawCommand : rawCommands) {

            String commandText =
                    rawCommand.trim();

            if (commandText.isBlank()) {
                continue;
            }

            String[] tokens =
                    commandText.split("\\s+");

            CommandsList type =
                    CommandsList.fromCommand(
                            tokens[0]
                    );

            List<String> arguments =
                    Arrays.stream(tokens)
                            .skip(1)
                            .toList();

            commands.add(
                    new Command(
                            type,
                            arguments
                    )
            );
        }

        if (commands.isEmpty()) {

            throw new IllegalArgumentException(
                    "Pipeline cannot be empty"
            );
        }

        return commands;
    }
}
