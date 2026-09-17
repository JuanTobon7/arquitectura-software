package com.pipesfilters.io;

import com.pipesfilters.dtos.FileFrame;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public final class DirectoryIO {

    public static final String DEFAULT_RESULTS_DIR = "result";

    private DirectoryIO() {
    }

    public static Path resolveOutputDir(String subdir) {
        Path projectRoot = Paths.get(".").toAbsolutePath().normalize();
        Path timestamped = projectRoot
                .resolve(DEFAULT_RESULTS_DIR)
                .resolve(subdir)
                .resolve(subdir + "-output-" + System.currentTimeMillis());

        try {
            Files.createDirectories(timestamped);
        } catch (IOException e) {
            throw new RuntimeException(
                    "No se pudo crear el directorio de salida: " + timestamped,
                    e
            );
        }

        return timestamped;
    }

    public static String readDirectoryPath(InputStream input) throws IOException {
        String line = new java.io.BufferedReader(
                new java.io.InputStreamReader(input, StandardCharsets.UTF_8)
        ).readLine();

        if (line == null || line.isBlank()) {
            throw new IllegalArgumentException(
                    "Debe proporcionar la ruta del directorio por stdin"
            );
        }

        return line.trim();
    }

    public static void writeDirectoryPath(OutputStream output, Path directory)
            throws IOException {
        String path = directory.toAbsolutePath().toString();
        output.write(path.getBytes(StandardCharsets.UTF_8));
        output.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));
        output.flush();
    }

    public static InputStream toFpsStream(Path directory) throws IOException {
        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();

        try (Stream<Path> paths = Files.list(directory)) {
            paths
                    .filter(Files::isRegularFile)
                    .sorted()
                    .forEach(file -> writeFrame(buffer, file));
        }

        writeInt(buffer, 0);
        buffer.flush();

        return new ByteArrayInputStream(buffer.toByteArray());
    }

    public static void writeAllFrames(OutputStream output, Path directory)
            throws IOException {
        try (Stream<Path> paths = Files.list(directory)) {
            for (Path file : paths.filter(Files::isRegularFile).sorted().toList()) {
                byte[] content = Files.readAllBytes(file);
                FileFrame frame = new FileFrame(
                        file.getFileName().toString(),
                        content
                );
                writeFrame(output, frame);
            }
        }
        writeInt(output, 0);
    }

    public static void writeFrame(OutputStream output, FileFrame frame)
            throws IOException {
        byte[] nameBytes = frame.name().getBytes(StandardCharsets.UTF_8);
        byte[] content = frame.content();

        writeInt(output, 0x46505331);
        writeInt(output, nameBytes.length);
        output.write(nameBytes);
        writeLong(output, content.length);
        output.write(content);
    }

    private static void writeFrame(java.io.ByteArrayOutputStream buffer, Path file) {
        try {
            String name = file.getFileName().toString();
            byte[] content = Files.readAllBytes(file);
            FileFrame frame = new FileFrame(name, content);

            byte[] nameBytes = frame.name().getBytes(StandardCharsets.UTF_8);

            writeInt(buffer, 0x46505331);
            writeInt(buffer, nameBytes.length);
            buffer.write(nameBytes);
            writeLong(buffer, frame.content().length);
            buffer.write(frame.content());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeInt(OutputStream out, int value) throws IOException {
        out.write((value >>> 24) & 0xFF);
        out.write((value >>> 16) & 0xFF);
        out.write((value >>> 8) & 0xFF);
        out.write(value & 0xFF);
    }

    private static void writeLong(OutputStream out, long value) throws IOException {
        out.write((int) (value >>> 56) & 0xFF);
        out.write((int) (value >>> 48) & 0xFF);
        out.write((int) (value >>> 40) & 0xFF);
        out.write((int) (value >>> 32) & 0xFF);
        out.write((int) (value >>> 24) & 0xFF);
        out.write((int) (value >>> 16) & 0xFF);
        out.write((int) (value >>> 8) & 0xFF);
        out.write((int) value & 0xFF);
    }
}
