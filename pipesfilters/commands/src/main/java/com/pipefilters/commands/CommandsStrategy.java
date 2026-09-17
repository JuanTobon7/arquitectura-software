package com.pipefilters.commands;

import com.pipefilters.converter.Base64ToBinary;
import com.pipefilters.converter.BinaryToBase64;
import com.pipefilters.persistence.Persistence;
import com.pipefilters.security.EncryptSha256;
import com.pipesfilters.images.ProcessorImages;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class CommandsStrategy {

    private final Map<CommandsList, Supplier<Object>> filters;

    public CommandsStrategy() {

        this.filters = Map.of(
                CommandsList.IMAGE, ProcessorImages::new,
                CommandsList.BASE64, BinaryToBase64::new,
                CommandsList.BINARY, Base64ToBinary::new,
                CommandsList.SECURITY, EncryptSha256::new,
                CommandsList.SAVE, Persistence::new
        );
    }

    public List<Object> buildPipeline(
            List<CommandsList> commands
    ) {

        validateOrder(commands);

        List<Object> pipeline = new ArrayList<>();

        for (CommandsList command : commands) {

            Supplier<Object> filter =
                    filters.get(command);

            if (filter == null) {
                throw new IllegalArgumentException(
                        "Unknown command: " + command
                );
            }

            pipeline.add(filter.get());
        }

        return pipeline;
    }

    private void validateOrder(
            List<CommandsList> commands
    ) {

        for (int i = 1; i < commands.size(); i++) {

            CommandsList previous = commands.get(i - 1);
            CommandsList current = commands.get(i);

            if (current.getOrder() < previous.getOrder()) {
                throw new IllegalArgumentException(
                        "Invalid pipeline order: "
                                + previous.getCommand()
                                + " -> "
                                + current.getCommand()
                );
            }
        }
    }
}