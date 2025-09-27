package org.example.node.service.collection;

import org.example.node.dto.collection.CollectionMeta;
import org.example.node.dto.collection.CollectionRequest;
import org.example.node.dto.database.DirectoryRenameRequest;
import org.example.node.util.filesystem.DirectoryUtil;
import org.example.node.util.filesystem.JsonPayloadUtil;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

@Service
public class CollectionManagementService {

    public static final String DOCUMENTS = "documents";

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
        Path documentsPath = collectionPath.resolve(DOCUMENTS);
        DirectoryUtil.createDirectory(documentsPath);
    }

}
