package org.example.node.events.document;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.node.locks.ConsulLockService;
import org.example.node.locks.SpringContext;
import org.example.node.repository.JsonRepository;
import org.example.node.service.queries.DocumentDeletionManager;
import org.example.node.service.queries.DocumentUpdaterService;
import org.example.node.service.indexing.IndexUpdaterService;
import org.example.node.service.indexing.JsonIndexingService;
import org.example.node.util.filesystem.PathUtil;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.HashSet;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class DeleteDocumentEvent extends DocumentEvent implements Serializable {
    public static final String COULD_NOT_ACQUIRE_LOCK_FOR_DOCUMENT = "Could not acquire lock for document ";
    private JsonNode flattenedSchema;
    private HashSet<String> filteringResults;

    @Override
    public void process(JsonIndexingService jis, JsonRepository jsr, DocumentDeletionManager ddm, DocumentUpdaterService dus, IndexUpdaterService ius) throws IOException {
        ConsulLockService lockService = SpringContext.getBean(ConsulLockService.class);

        HashSet<String> acquiredLocks = new HashSet<>();
        try {
            for (String docId : filteringResults) {
                String lockKey = "doc_" + getUserFolderName() + "_" + getDatabaseName() + "_" + getCollectionName() + "_" + docId;
                boolean lockAcquired = lockService.tryAcquireWithRetry(() -> lockService.acquireWriteLock(lockKey), 10);
                if (!lockAcquired) {
                    throw new IOException(COULD_NOT_ACQUIRE_LOCK_FOR_DOCUMENT + docId);
                }
                acquiredLocks.add(lockKey);
            }

            Path collectionPath = PathUtil.buildPath(getUserFolderName(), getDatabaseName(), getCollectionName());
            ddm.deleteDocuments(filteringResults, flattenedSchema, collectionPath);

        } finally {
            for (String lockKey : acquiredLocks) {
                lockService.releaseWriteLock(lockKey);
            }
        }
    }

}
