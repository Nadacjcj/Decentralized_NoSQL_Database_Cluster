package org.example.node.service;

import org.example.node.dto.*;
import org.example.node.util.DirectoryUtil;
import org.example.node.util.JsonPayloadUtil;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
public class CollectionManagementService {

    public void createFolder(Path collectionPath , String username , String databaseName, CollectionMeta collectionMeta) throws IOException {
       DirectoryUtil.createDirectory(collectionPath);
       createDocumentsDirectory(collectionPath);
    }

    public boolean deleteFolder(Path collectionPath , String username , String databaseName, CollectionRequest collectionRequest ) throws IOException {
        DirectoryUtil.deleteDirectory(collectionPath);
        return true;
    }

    public boolean renameFolder(Path collectionPath , String username , String databaseName, DirectoryRenameRequest collectionRenameRequest) throws IOException {
        boolean oldIndexExists = checkKeyExistence(username , databaseName,collectionRenameRequest.getOldDirectoryName());
        boolean newIndexExists = checkKeyExistence(username , databaseName,collectionRenameRequest.getNewDirectoryName());

        if (collectionRenameRequest.getOldDirectoryName().isEmpty() || !oldIndexExists) {
            return false;
        }
        if (collectionRenameRequest.getNewDirectoryName().isEmpty() || newIndexExists) {
            return false;
        }
        Path newPath = collectionPath.resolveSibling(collectionRenameRequest.getNewDirectoryName());
        DirectoryUtil.renameDirectory(collectionPath, newPath);
        return  true;
    }

    public boolean checkKeyExistence(String username , String databaseName , String collectionName) throws IOException {
        return JsonPayloadUtil.loadCollectionIndex(username , databaseName).containsKey(collectionName);
    }

    private void createDocumentsDirectory(Path collectionPath) throws IOException {
        Path documentsPath = collectionPath.resolve("documents");
        DirectoryUtil.createDirectory(documentsPath);
    }

}
