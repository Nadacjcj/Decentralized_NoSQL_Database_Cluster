package org.example.node.events;

import org.example.node.dto.DirectoryRenameRequest;
import org.example.node.service.CollectionIndexService;
import org.example.node.service.CollectionManagementService;
import org.example.node.service.JsonIndexingService;
import org.example.node.util.PathUtil;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.nio.file.Path;

public class RenameCollectionEvent extends CollectionEvent implements Serializable {
    private DirectoryRenameRequest request;

    public RenameCollectionEvent(){}
    public DirectoryRenameRequest getRequest() {
        return request;
    }
    public void setRequest(DirectoryRenameRequest request) {
        this.request = request;
    }

    @Override
    public void process(CollectionManagementService cms,
                        CollectionIndexService cis,
                        JsonIndexingService jis) throws IOException {

        Path collectionPath = PathUtil.buildPath(getUserFolderName(),
                getDatabaseName(),
                request.getOldDirectoryName());

        cms.renameFolder(collectionPath, getUserFolderName(), getDatabaseName(), request);
        Path metaFilePath = PathUtil.buildPath(getUserFolderName(), getDatabaseName())
                .resolve("collections_info.json");
        cis.renameCollection(request, metaFilePath);
    }

    @Override
    public String toString() {
        return "RenameCollectionEvent{" +
                "request=" + request +
                '}';
    }
}
