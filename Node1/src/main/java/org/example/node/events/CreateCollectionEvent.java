package org.example.node.events;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.node.dto.CollectionMeta;
import org.example.node.dto.CollectionRequest;
import org.example.node.service.CollectionIndexService;
import org.example.node.service.CollectionManagementService;
import org.example.node.service.JsonIndexingService;
import org.example.node.util.PathUtil;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;

public class CreateCollectionEvent extends CollectionEvent implements Serializable {
    private CollectionMeta collectionMeta;
    private CollectionRequest collectionRequest;
    private JsonNode flattenedSchema;

    public CreateCollectionEvent(){}

    public CollectionMeta getCollectionMeta() {
        return collectionMeta;
    }

    public void setCollectionMeta(CollectionMeta collectionMeta) {
        this.collectionMeta = collectionMeta;
    }

    public CollectionRequest getCollectionRequest() {
        return collectionRequest;
    }

    public JsonNode getFlattenedSchema() {
        return flattenedSchema;
    }

    public void setFlattenedSchema(JsonNode flattenedSchema) {
        this.flattenedSchema = flattenedSchema;
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

        cms.createFolder(collectionPath, getUserFolderName(), getDatabaseName(), collectionMeta);
        Path metaFilePath = PathUtil.buildPath(getUserFolderName(), getDatabaseName())
                .resolve("collections_info.json");
        cis.storeCollectionInfo(collectionMeta, metaFilePath);
        jis.checkIndexableFields(flattenedSchema, collectionPath);
    }
}
