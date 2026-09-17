package com.pipefilters.commands.components;

import com.pipefilters.commands.Command;

import java.util.List;

public record ParsedCommand(
        String input,
        List<Command> commands
) {
}