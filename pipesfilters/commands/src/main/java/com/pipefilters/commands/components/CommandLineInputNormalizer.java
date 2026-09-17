package com.pipefilters.commands.components;

import com.pipefilters.commands.Command;
import com.pipefilters.commands.CommandsList;

import java.util.List;

public final class CommandLineInputNormalizer {

    private CommandLineInputNormalizer() {
    }

    public static ParsedCommand parse(String commandLine) {
        return CommandBuilder.parse(commandLine);
    }

    public static String normalizeInput(String value) {
        if (value == null) {
            return null;
        }

        if (value.length() >= 2
                && value.startsWith("\"")
                && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }

        return value;
    }

    public static String getImageExtension(List<Command> commands) {
        for (Command command : commands) {
            if (command.type() != CommandsList.IMAGE) {
                continue;
            }

            if (command.arguments().isEmpty()) {
                return null;
            }

            return command.arguments().getFirst();
        }

        return null;
    }
}
