package org.example.node.events;

import org.example.node.dto.CollectionRequest;
import org.example.node.service.CollectionIndexService;
import org.example.node.service.CollectionManagementService;
import org.example.node.service.JsonIndexingService;
import org.example.node.util.PathUtil;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;

public class DeleteCollectionEvent extends CollectionEvent implements Serializable {
    private CollectionRequest collectionRequest;

    public DeleteCollectionEvent(){}
    public CollectionRequest getCollectionRequest() {
        return collectionRequest;
    }

    public void setCollectionRequest(CollectionRequest collectionRequest) {
        this.collectionRequest = collectionRequest;
    }
    @Override
    public void process(CollectionManagementService cms,
                        CollectionIndexService cis,
                        JsonIndexingService jis) throws IOException {
        Path collectionPath = PathUtil.buildPath(getUserFolderName(),
                getDatabaseName(),
                collectionRequest.getCollectionName());

        cms.deleteFolder(collectionPath, getUserFolderName(), getDatabaseName(), collectionRequest);
        Path metaFilePath = PathUtil.buildPath(getUserFolderName(), getDatabaseName())
                .resolve("collections_info.json");
        cis.removeCollectionInfo(collectionRequest.getCollectionName(), metaFilePath);
    }

}
