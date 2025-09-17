package org.example.node.events;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.node.dto.UpdateDocRequest;
import org.example.node.locks.ConsulLockService;
import org.example.node.locks.SpringContext;
import org.example.node.repository.JsonRepository;
import org.example.node.service.DocumentDeletionManager;
import org.example.node.service.DocumentUpdaterService;
import org.example.node.service.IndexUpdaterService;
import org.example.node.service.JsonIndexingService;
import org.example.node.util.PathUtil;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.HashSet;

public class UpdateDocumentEvent extends DocumentEvent implements Serializable {
    private JsonNode flattenedSchema;
    private HashSet<String> filteringResults;
    private UpdateDocRequest updateDocRequest;

    public JsonNode getFlattenedSchema() {
        return flattenedSchema;
    }

    public void setFlattenedSchema(JsonNode flattenedSchema) {
        this.flattenedSchema = flattenedSchema;
    }

    public HashSet<String> getFilteringResults() {
        return filteringResults;
    }

    public void setFilteringResults(HashSet<String> filteringResults) {
        this.filteringResults = filteringResults;
    }

    public UpdateDocRequest getUpdateDocRequest() {
        return updateDocRequest;
    }

    public void setUpdateDocRequest(UpdateDocRequest updateDocRequest) {
        this.updateDocRequest = updateDocRequest;
    }

    @Override
    public void process(JsonIndexingService jis, JsonRepository jsr, DocumentDeletionManager ddm, DocumentUpdaterService dus, IndexUpdaterService ius) throws IOException {
        ConsulLockService lockService = SpringContext.getBean(ConsulLockService.class);

        HashSet<String> acquiredLocks = new HashSet<>();
        try {
            for (String docId : filteringResults) {
                String lockKey = "doc:" + getUserFolderName() + ":" + getDatabaseName() + ":" + getCollectionName() + ":" + docId;
                boolean lockAcquired = lockService.tryAcquireWithRetry(() -> lockService.acquireWriteLock(lockKey), 10);
                if (!lockAcquired) {
                    throw new IOException("Could not acquire lock for document " + docId);
                }
                acquiredLocks.add(lockKey);
            }

            Path collectionPath = PathUtil.buildPath(getUserFolderName(), getDatabaseName(), getCollectionName());
            Path documentsPath = collectionPath.resolve("documents");

            ius.updateIndexes(updateDocRequest, filteringResults, flattenedSchema, collectionPath, documentsPath);
            dus.updateDocuments(updateDocRequest, filteringResults, documentsPath);

        } finally {
            for (String lockKey : acquiredLocks) {
                lockService.releaseWriteLock(lockKey);
            }
        }
    }

}
