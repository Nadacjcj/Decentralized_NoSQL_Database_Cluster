package org.example.node.events.collection;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.node.dto.database.DirectoryRenameRequest;
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
public class RenameCollectionEvent extends CollectionEvent implements Serializable {
    public static final String COLLECTIONS_INFO_JSON = "collections_info.json";
    private DirectoryRenameRequest request;

    @Override
    public void process(CollectionManagementService cms,
                        CollectionIndexService cis,
                        JsonIndexingService jis) throws IOException {

        Path collectionPath = PathUtil.buildPath(getUserFolderName(),
                getDatabaseName(),
                request.getOldDirectoryName());

        cms.renameFolder(collectionPath, getUserFolderName(), getDatabaseName(), request);
        Path metaFilePath = PathUtil.buildPath(getUserFolderName(), getDatabaseName())
                .resolve(COLLECTIONS_INFO_JSON);
        cis.renameCollection(request, metaFilePath);
    }
}
