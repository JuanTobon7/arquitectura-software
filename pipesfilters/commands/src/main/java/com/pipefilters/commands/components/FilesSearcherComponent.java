package com.pipefilters.commands.components;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class FilesSearcherComponent {

    public static List<Path> search(
            String location,
            String extension
    ) {

        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException(
                    "File location cannot be empty"
            );
        }

        Path path = Path.of(location);

        if (!Files.exists(path)) {
            throw new IllegalArgumentException(
                    "Path does not exist: " + location
            );
        }

        if (Files.isRegularFile(path)) {

            if (extension == null ||
                    matchesExtension(path, extension)) {

                return List.of(path);
            }

            return List.of();
        }

        if (Files.isDirectory(path)) {
            return searchDirectory(path, extension);
        }

        throw new IllegalArgumentException(
                "Path is neither a file nor a directory: " + location
        );
    }

    private static List<Path> searchDirectory(
            Path directory,
            String extension
    ) {

        try (Stream<Path> files = Files.walk(directory)) {

            return files
                    .filter(Files::isRegularFile)
                    .filter(path ->
                            extension == null ||
                                    matchesExtension(path, extension)
                    )
                    .toList();

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Could not read directory: " + directory,
                    e
            );
        }
    }

    private static boolean matchesExtension(
            Path path,
            String extension
    ) {

        String normalizedExtension =
                extension
                        .toLowerCase(Locale.ROOT)
                        .replace(".", "");

        String fileName =
                path.getFileName()
                        .toString()
                        .toLowerCase(Locale.ROOT);

        return fileName.endsWith(
                "." + normalizedExtension
        );
    }
}