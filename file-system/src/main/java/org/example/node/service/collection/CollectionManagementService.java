package org.example.node.service.collection;

import org.example.node.dto.collection.CollectionMeta;
import org.example.node.dto.collection.CollectionRequest;
import org.example.node.dto.database.DirectoryRenameRequest;
import org.example.node.util.filesystem.DirectoryUtil;
import org.example.node.util.filesystem.JsonPayloadUtil;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Service
public class CollectionManagementService {

    private static final Logger logger = LogManager.getLogger(CollectionManagementService.class);

    public static final String DOCUMENTS = "documents";

    public void createFolder(Path collectionPath, String username, String databaseName, CollectionMeta collectionMeta) throws IOException {
        DirectoryUtil.createDirectory(collectionPath);
        createDocumentsDirectory(collectionPath);
        logger.info("Created folder at path {}", collectionPath);
    }

    public boolean deleteFolder(Path collectionPath, String username, String databaseName, CollectionRequest collectionRequest) throws IOException {
        DirectoryUtil.deleteDirectory(collectionPath);
        logger.info("Deleted folder at path {}", collectionPath);
        return true;
    }

    public boolean renameFolder(Path collectionPath, String username, String databaseName, DirectoryRenameRequest collectionRenameRequest) throws IOException {
        boolean oldIndexExists = checkKeyExistence(username, databaseName, collectionRenameRequest.getOldDirectoryName());
        boolean newIndexExists = checkKeyExistence(username, databaseName, collectionRenameRequest.getNewDirectoryName());

        if (collectionRenameRequest.getOldDirectoryName().isEmpty() || !oldIndexExists) {
            logger.warn("Old directory '{}' does not exist", collectionRenameRequest.getOldDirectoryName());
            return false;
        }
        if (collectionRenameRequest.getNewDirectoryName().isEmpty() || newIndexExists) {
            logger.warn("New directory '{}' already exists", collectionRenameRequest.getNewDirectoryName());
            return false;
        }
        Path newPath = collectionPath.resolveSibling(collectionRenameRequest.getNewDirectoryName());
        DirectoryUtil.renameDirectory(collectionPath, newPath);
        logger.info("Renamed folder '{}' to '{}'", collectionPath, newPath);
        return true;
    }

    public boolean checkKeyExistence(String username, String databaseName, String collectionName) throws IOException {
        return JsonPayloadUtil.loadCollectionIndex(username, databaseName).containsKey(collectionName);
    }

    private void createDocumentsDirectory(Path collectionPath) throws IOException {
        Path documentsPath = collectionPath.resolve(DOCUMENTS);
        DirectoryUtil.createDirectory(documentsPath);
        logger.info("Created documents directory at {}", documentsPath);
    }

}
