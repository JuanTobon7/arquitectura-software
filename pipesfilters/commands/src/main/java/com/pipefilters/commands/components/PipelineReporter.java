package com.pipefilters.commands.components;

import com.pipefilters.commands.Command;

import java.nio.file.Path;
import java.util.List;

public final class PipelineReporter {

    private PipelineReporter() {
    }

    public static void printNoFilesFound(String extension) {
        System.out.println(
                "No files found"
                        + (extension != null ? " with extension ." + extension : "")
        );
    }

    public static void printPipeline(String input, List<Path> files, List<Command> commands) {
        System.out.println("Input: " + input);
        System.out.println("Files found: " + files.size());

        for (Path file : files) {
            System.out.println("  - " + file);
        }

        System.out.println("Pipeline:");

        for (Command command : commands) {
            System.out.println(
                    "  | "
                            + command.type().getCommand()
                            + formatArguments(command)
            );
        }
    }

    private static String formatArguments(Command command) {
        if (command.arguments().isEmpty()) {
            return "";
        }

        return " " + String.join(" ", command.arguments());
    }
}
