package org.example.node.events;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.node.repository.JsonRepository;
import org.example.node.service.DocumentDeletionManager;
import org.example.node.service.DocumentUpdaterService;
import org.example.node.service.IndexUpdaterService;
import org.example.node.service.JsonIndexingService;
import org.example.node.util.JsonPayloadUtil;
import org.example.node.util.PathUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.print.Doc;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;

public class InsertDocumentEvent extends DocumentEvent implements Serializable {
    private JsonNode Schema , flattenedDocument , finalDocument;

    public InsertDocumentEvent() {
    }
    public JsonNode getSchema() {
        return Schema;
    }

    public void setSchema(JsonNode schema) {
        Schema = schema;
    }

    public JsonNode getFlattenedDocument() {
        return flattenedDocument;
    }

    public void setFlattenedDocument(JsonNode flattenedDocument) {
        this.flattenedDocument = flattenedDocument;
    }

    public JsonNode getFinalDocument() {
        return finalDocument;
    }

    public void setFinalDocument(JsonNode finalDocument) {
        this.finalDocument = finalDocument;
    }

    @Override
    public void process(JsonIndexingService jis , JsonRepository jsr , DocumentDeletionManager ddm , DocumentUpdaterService dus , IndexUpdaterService ius) throws IOException {
        Path documentPath = PathUtil.buildPath(getUserFolderName() , getDatabaseName() , getCollectionName()).resolve("documents");
        Path collectionPath = PathUtil.buildPath(getUserFolderName() , getDatabaseName() , getCollectionName());

        saveDocument(documentPath, finalDocument , jsr);
        jis.updateIndexes(getSchema() , getFlattenedDocument() , collectionPath);
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
