package org.example.node.events;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.node.repository.JsonRepository;
import org.example.node.service.DocumentDeletionManager;
import org.example.node.service.DocumentUpdaterService;
import org.example.node.service.IndexUpdaterService;
import org.example.node.service.JsonIndexingService;
import org.example.node.util.PathUtil;

import javax.print.Doc;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.HashSet;

public class DeleteDocumentEvent extends DocumentEvent implements Serializable {
    private JsonNode flattenedSchema;
    private HashSet<String> filteringResults;

    public DeleteDocumentEvent() {}

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

    @Override
    public void process(JsonIndexingService jis, JsonRepository jsr , DocumentDeletionManager ddm , DocumentUpdaterService dus , IndexUpdaterService ius) throws IOException {
        Path collectionPath = PathUtil.buildPath(getUserFolderName() , getDatabaseName() , getCollectionName());
        ddm.deleteDocuments(filteringResults, flattenedSchema, collectionPath);

    }
}
