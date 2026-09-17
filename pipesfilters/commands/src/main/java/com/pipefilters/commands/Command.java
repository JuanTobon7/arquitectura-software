package com.pipefilters.commands;

import java.util.List;

public record Command(
        CommandsList type,
        List<String> arguments
) {
}
