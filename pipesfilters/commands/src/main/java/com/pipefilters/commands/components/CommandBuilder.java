package com.pipefilters.commands.components;

import com.pipefilters.commands.Command;

import java.util.List;

public final class CommandBuilder {

    public static ParsedCommand parse(
            String commandLine
    ) {

        String[] inputAndPipeline =
                commandLine.split(">", 2);

        if (inputAndPipeline.length != 2) {

            throw new IllegalArgumentException(
                    "Expected: <input> > <pipeline>"
            );
        }

        String input =
                inputAndPipeline[0].trim();

        String pipelineText =
                inputAndPipeline[1].trim();

        if (input.isBlank()) {

            throw new IllegalArgumentException(
                    "Input cannot be empty"
            );
        }

        if (pipelineText.isBlank()) {

            throw new IllegalArgumentException(
                    "Pipeline cannot be empty"
            );
        }

        List<Command> commands =
                ParserPipeline.parse(pipelineText);

        return new ParsedCommand(
                input,
                commands
        );
    }
}
