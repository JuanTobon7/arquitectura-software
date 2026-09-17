package com.pipefilters.security;

import com.pipesfilters.dtos.FileFrame;
import com.pipesfilters.io.DirectoryIO;
import com.pipesfilters.io.FpsDirectoryExtractor;
import com.pipesfilters.protocol.FpsReader;
import com.pipesfilters.protocol.FpsWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class EncryptSha256Main {

    public static void main(String[] args) {

        try {
            byte[] allInput = System.in.readAllBytes();

            String inputDirPath = FpsDirectoryExtractor.extractDirectoryPath(
                    new ByteArrayInputStream(allInput)
            );

            Path inputDir = Path.of(inputDirPath);

            if (!Files.isDirectory(inputDir)) {
                throw new IllegalArgumentException(
                        "La ruta no es un directorio: " + inputDir
                );
            }

            Path outputDir = DirectoryIO.resolveOutputDir("security");

            EncryptSha256 filter = new EncryptSha256();

            ByteArrayOutputStream inputBuffer = new ByteArrayOutputStream();
            DirectoryIO.writeAllFrames(inputBuffer, inputDir);

            ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
            FpsReader reader = new FpsReader(
                    new ByteArrayInputStream(inputBuffer.toByteArray())
            );
            FpsWriter fileWriter = new FpsWriter(outputBuffer);

            filter.process(reader, fileWriter);

            FpsReader outReader = new FpsReader(
                    new ByteArrayInputStream(outputBuffer.toByteArray())
            );
            FileFrame frame;
            while ((frame = outReader.read()) != null) {
                Files.write(outputDir.resolve(frame.name()), frame.content());
            }

            FpsWriter stdoutWriter = new FpsWriter(System.out);
            String dirPath = outputDir.toAbsolutePath().toString();
            stdoutWriter.write(new FileFrame(
                    FpsDirectoryExtractor.DIRECTORY_MARKER,
                    dirPath.getBytes(StandardCharsets.UTF_8)
            ));
            stdoutWriter.close();

        } catch (Exception e) {
            System.err.println("EncryptSha256 error: " + e.getMessage());
            System.exit(1);
        }
    }
}