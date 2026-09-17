package com.pipefilters.commands.components;

import com.pipefilters.commands.Command;
import com.pipefilters.commands.CommandsStrategy;
import com.pipesfilters.filters.Filter;
import com.pipesfilters.protocol.FpsReader;
import com.pipesfilters.protocol.FpsWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CommandPipelineRunner {

    private final CommandsStrategy strategy;

    public CommandPipelineRunner(CommandsStrategy strategy) {
        this.strategy = strategy;
    }

    public void executeFile(Path file, List<Command> commands) throws Exception {
        List<Object> pipeline = strategy.buildPipeline(
                commands.stream()
                        .map(Command::type)
                        .toList()
        );

        ByteArrayInputStream input = new ByteArrayInputStream(
                buildFpsStream(file)
        );

        for (Object stage : pipeline) {
            if (!(stage instanceof Filter filter)) {
                throw new IllegalStateException(
                        "Invalid filter in pipeline: " + stage.getClass().getName()
                );
            }

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            FpsWriter writer = new FpsWriter(output);
            filter.process(new FpsReader(input), writer);
            input = new ByteArrayInputStream(output.toByteArray());
        }
    }

    private byte[] buildFpsStream(Path file) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        FpsWriter writer = new FpsWriter(buffer);
        writer.write(new com.pipesfilters.dtos.FileFrame(
                file.getFileName().toString(),
                Files.readAllBytes(file)
        ));
        writer.close();

        return buffer.toByteArray();
    }
}
