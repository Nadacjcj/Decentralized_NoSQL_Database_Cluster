package org.example.node.util.filesystem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PathUtil {

    private static final Logger logger = LogManager.getLogger(PathUtil.class);

    static final String basePath = "user_data";

    public static Path buildPath(String userDirectory) {
        Path folderPath = Paths.get(basePath, userDirectory).toAbsolutePath();
        logger.debug("Built path: {}", folderPath);
        return folderPath;
    }

    public static Path buildPath(String userDirectory, String dir1) {
        Path folderPath = Paths.get(basePath, userDirectory, dir1).toAbsolutePath();
        logger.debug("Built path: {}", folderPath);
        return folderPath;
    }

    public static Path buildPath(String userDirectory, String dir2, String dir3) {
        Path folderPath = Paths.get(basePath, userDirectory, dir2, dir3).toAbsolutePath();
        logger.debug("Built path: {}", folderPath);
        return folderPath;
    }
}
