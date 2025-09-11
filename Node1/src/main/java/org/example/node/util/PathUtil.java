package org.example.node.util;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

// This is responsible for building up a path to whatever location inside the user_data folder
// either for a user's directory , or a db directory or even a collection
public class PathUtil {
    static final String basePath = "user_data";

    public static Path buildPath(String userDirectory) {
        Path folderPath = Paths.get(basePath, userDirectory);
        return folderPath.toAbsolutePath();
    }

    public static Path buildPath(String userDirectory, String dir1) throws IOException {
        Path folderPath = Paths.get(basePath, userDirectory, dir1);
        return folderPath.toAbsolutePath();
    }

    public static Path buildPath(String userDirectory, String dir2, String dir3) {
        Path folderPath = Paths.get(basePath, userDirectory, dir2 , dir3);
        return folderPath.toAbsolutePath();
    }
}
