package org.example.node.util.filesystem;


import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class DirectoryUtil {
    public static void deleteDirectory(Path directoryPath) throws IOException {

        if (Files.exists(directoryPath)) {
            Files.walk(directoryPath)
                    .sorted((p1, p2) -> p2.compareTo(p1))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to delete " + path, e);
                        }
                    });
        }

    }

    public static void createDirectory(Path directoryPath) throws IOException {
        Files.createDirectories(directoryPath);
    }
    public static void renameDirectory(Path sourceDir, Path targetDir) throws IOException {
        if (!Files.exists(sourceDir)) {
            throw new IOException("Source directory does not exist: " + sourceDir);
        }
        if (Files.exists(targetDir)) {
            throw new IOException("Target directory already exists: " + targetDir);
        }
        Files.createDirectories(targetDir);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(sourceDir)) {
            for (Path entry : stream) {
                Path targetPath = targetDir.resolve(entry.getFileName());
                if (Files.isDirectory(entry)) {
                    renameDirectory(entry, targetPath);
                } else {
                    Files.move(entry, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }

        Files.delete(sourceDir);
    }


}
