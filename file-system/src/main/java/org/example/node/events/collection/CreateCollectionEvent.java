package org.example.node.events.collection;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.node.dto.collection.CollectionMeta;
import org.example.node.dto.collection.CollectionRequest;
import org.example.node.service.collection.CollectionIndexService;
import org.example.node.service.collection.CollectionManagementService;
import org.example.node.service.indexing.JsonIndexingService;
import org.example.node.util.filesystem.PathUtil;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class CreateCollectionEvent extends CollectionEvent implements Serializable {
    public static final String COLLECTIONS_INFO_JSON = "collections_info.json";
    private CollectionMeta collectionMeta;
    private CollectionRequest collectionRequest;
    private JsonNode flattenedSchema;

    @Override
    public void process(CollectionManagementService cms,
                        CollectionIndexService cis,
                        JsonIndexingService jis) throws IOException {
        Path collectionPath = PathUtil.buildPath(getUserFolderName(),
                getDatabaseName(),
                collectionRequest.getCollectionName());

        cms.createFolder(collectionPath, getUserFolderName(), getDatabaseName(), collectionMeta);
        Path metaFilePath = PathUtil.buildPath(getUserFolderName(), getDatabaseName())
                .resolve(COLLECTIONS_INFO_JSON);
        cis.storeCollectionInfo(collectionMeta, metaFilePath);
        jis.checkIndexableFields(flattenedSchema, collectionPath);
    }
}
