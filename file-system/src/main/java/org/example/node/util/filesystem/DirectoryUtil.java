package org.example.node.util.filesystem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class DirectoryUtil {

    private static final Logger logger = LogManager.getLogger(DirectoryUtil.class);

    public static void deleteDirectory(Path directoryPath) throws IOException {
        if (Files.exists(directoryPath)) {
            Files.walk(directoryPath)
                    .sorted((p1, p2) -> p2.compareTo(p1))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            logger.debug("Deleted path: {}", path);
                        } catch (IOException e) {
                            logger.error("Failed to delete {}", path, e);
                            throw new RuntimeException("Failed to delete " + path, e);
                        }
                    });
            logger.info("Deleted directory: {}", directoryPath);
        }
    }

    public static void createDirectory(Path directoryPath) throws IOException {
        Files.createDirectories(directoryPath);
        logger.info("Created directory: {}", directoryPath);
    }

    public static void renameDirectory(Path sourceDir, Path targetDir) throws IOException {
        if (!Files.exists(sourceDir)) {
            logger.error("Source directory does not exist: {}", sourceDir);
            throw new IOException("Source directory does not exist: " + sourceDir);
        }
        if (Files.exists(targetDir)) {
            logger.error("Target directory already exists: {}", targetDir);
            throw new IOException("Target directory already exists: " + targetDir);
        }
        Files.createDirectories(targetDir);
        logger.debug("Created target directory: {}", targetDir);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(sourceDir)) {
            for (Path entry : stream) {
                Path targetPath = targetDir.resolve(entry.getFileName());
                if (Files.isDirectory(entry)) {
                    renameDirectory(entry, targetPath);
                } else {
                    Files.move(entry, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    logger.debug("Moved file {} to {}", entry, targetPath);
                }
            }
        }

        Files.delete(sourceDir);
        logger.info("Renamed directory from {} to {}", sourceDir, targetDir);
    }
}
