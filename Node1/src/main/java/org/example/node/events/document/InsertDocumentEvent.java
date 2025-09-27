package org.example.node.events.document;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import org.example.node.util.filesystem.JsonPayloadUtil;
import org.example.node.util.filesystem.PathUtil;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class InsertDocumentEvent extends DocumentEvent implements Serializable {
    public static final String COULD_NOT_ACQUIRE_LOCK_FOR_COLLECTION = "Could not acquire lock for collection ";
    public static final String DOCUMENTS = "documents";
    private JsonNode Schema , flattenedDocument , finalDocument;

    @Override
    public void process(JsonIndexingService jis , JsonRepository jsr , DocumentDeletionManager ddm , DocumentUpdaterService dus , IndexUpdaterService ius) throws IOException {
        ConsulLockService lockService = SpringContext.getBean(ConsulLockService.class);

        String lockKey = "collection_" + getUserFolderName() + "_" + getDatabaseName() + "_" + getCollectionName();
        boolean lockAcquired = lockService.tryAcquireWithRetry(() -> lockService.acquireWriteLock(lockKey), 10);
        if (!lockAcquired) {
            throw new IOException(COULD_NOT_ACQUIRE_LOCK_FOR_COLLECTION + lockKey);
        }

        try {
            Path documentPath = PathUtil.buildPath(getUserFolderName(), getDatabaseName(), getCollectionName()).resolve(DOCUMENTS);
            Path collectionPath = PathUtil.buildPath(getUserFolderName(), getDatabaseName(), getCollectionName());

            saveDocument(documentPath, finalDocument, jsr);
            jis.updateIndexes(getSchema(), getFlattenedDocument(), collectionPath);
        } finally {
            lockService.releaseWriteLock(lockKey);
        }
    }

    private void saveDocument(Path documentPath , JsonNode document , JsonRepository jsr) throws IOException {
        String fileName = toJson(document);
        Path filePath = documentPath.resolve(fileName);

        JsonPayloadUtil.createEmptyJsonFileIfNotExists(filePath.toFile());
        jsr.writeIndex((ObjectNode) document, filePath);
    }
    private String toJson(JsonNode document){
        return  "doc_" + document.get("id").asText() + ".json";
    }
}
