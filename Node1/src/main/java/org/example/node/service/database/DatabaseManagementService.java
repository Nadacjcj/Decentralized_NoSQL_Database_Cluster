package org.example.node.service.database;

import org.example.node.dto.database.DBDeletionRequest;
import org.example.node.dto.database.DirectoryRenameRequest;
import org.example.node.dto.database.DBCreationRequest;
import org.example.node.util.filesystem.DirectoryUtil;
import org.example.node.util.filesystem.JsonPayloadUtil;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
// This is meant to handle folder creation , deletion , and renaming on whatever path given
@Service
public class DatabaseManagementService {


    public boolean createFolder(Path dbPath , String username , DBCreationRequest request) throws IOException {
        boolean indexExists = checkKeyExistence(username ,request.getDatabaseName());
        if (request.getDatabaseName().isEmpty() || indexExists) {
            return false;
        }
        DirectoryUtil.createDirectory(dbPath);
        return true;
    }

    public boolean deleteFolder(Path dbPath , String username , DBDeletionRequest request ) throws IOException {
        boolean indexExists = checkKeyExistence(username ,request.getDatabaseName());
        if (request.getDatabaseName().isEmpty() || !indexExists) {
            return false;
        }
        DirectoryUtil.deleteDirectory(dbPath);
        return true;
    }

    public boolean renameFolder(Path dbPath , String username, DirectoryRenameRequest databaseRenameRequest) throws IOException {
        boolean indexExists = checkKeyExistence(username ,databaseRenameRequest.getNewDirectoryName());
        if (databaseRenameRequest.getOldDirectoryName().isEmpty() || indexExists) {
            return false;
        }
        Path newPath = dbPath.resolveSibling(databaseRenameRequest.getNewDirectoryName());
        DirectoryUtil.renameDirectory(dbPath, newPath);
        return true;
    }

    public boolean checkKeyExistence(String username , String folderName) throws IOException {
        return JsonPayloadUtil.loadDatabasesIndex(username).containsKey(folderName);
    }
}
