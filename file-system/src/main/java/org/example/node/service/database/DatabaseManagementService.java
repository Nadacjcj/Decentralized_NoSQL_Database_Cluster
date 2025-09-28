package org.example.node.service.database;

import org.example.node.dto.database.DBCreationRequest;
import org.example.node.dto.database.DBDeletionRequest;
import org.example.node.dto.database.DirectoryRenameRequest;
import org.example.node.util.filesystem.DirectoryUtil;
import org.example.node.util.filesystem.JsonPayloadUtil;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Service
public class DatabaseManagementService {

    private static final Logger logger = LogManager.getLogger(DatabaseManagementService.class);

    public boolean createFolder(Path dbPath , String username , DBCreationRequest request) throws IOException {
        boolean indexExists = checkKeyExistence(username, request.getDatabaseName());
        if (request.getDatabaseName().isEmpty() || indexExists) {
            return false;
        }
        DirectoryUtil.createDirectory(dbPath);
        logger.info("Created database folder '{}' for user '{}'", request.getDatabaseName(), username);
        return true;
    }

    public boolean deleteFolder(Path dbPath , String username , DBDeletionRequest request) throws IOException {
        boolean indexExists = checkKeyExistence(username, request.getDatabaseName());
        if (request.getDatabaseName().isEmpty() || !indexExists) {
            return false;
        }
        DirectoryUtil.deleteDirectory(dbPath);
        logger.info("Deleted database folder '{}' for user '{}'", request.getDatabaseName(), username);
        return true;
    }

    public boolean renameFolder(Path dbPath , String username, DirectoryRenameRequest databaseRenameRequest) throws IOException {
        boolean indexExists = checkKeyExistence(username, databaseRenameRequest.getNewDirectoryName());
        if (databaseRenameRequest.getOldDirectoryName().isEmpty() || indexExists) {
            return false;
        }
        Path newPath = dbPath.resolveSibling(databaseRenameRequest.getNewDirectoryName());
        DirectoryUtil.renameDirectory(dbPath, newPath);
        logger.info("Renamed database folder '{}' to '{}' for user '{}'", databaseRenameRequest.getOldDirectoryName(), databaseRenameRequest.getNewDirectoryName(), username);
        return true;
    }

    public boolean checkKeyExistence(String username , String folderName) throws IOException {
        return JsonPayloadUtil.loadDatabasesIndex(username).containsKey(folderName);
    }
}
