package org.example.node.service.queries;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.node.events.document.InsertDocumentEvent;
import org.example.node.filter.JwtAuthenticationFilter;
import org.example.node.repository.JsonRepository;
import org.example.node.service.collection.SchemaValidationService;
import org.example.node.util.filesystem.JsonFileLister;
import org.example.node.util.filesystem.PathUtil;
import org.example.node.util.jsonflattener.DocumentFlattenStrategy;
import org.example.node.util.jsonflattener.JsonFlattener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.UUID;

@Service
public class InsertDocumentService {

    private static final Logger logger = LogManager.getLogger(InsertDocumentService.class);

    @Autowired
    private JsonFlattener jsonFlattener;
    @Autowired
    private SchemaValidationService schemaValidationService;
    @Autowired
    private JsonRepository jsonRepository;
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    private final ObjectMapper mapper;

    public InsertDocumentService(ObjectMapper mapper) {
        this.mapper = mapper;
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true);
    }

    public String insertDocument(JwtAuthenticationFilter.UserPrincipal user, String databaseName, String collectionName, JsonNode document) throws IOException {
        String userFolder = buildUserFolderName(user);

        Path collectionPath = PathUtil.buildPath(userFolder, databaseName, collectionName);
        Path documentPath = collectionPath.resolve("documents");
        Path schemaPath = collectionPath.resolve("schema.json");

        JsonNode schema = JsonRepository.readIndex(schemaPath);
        JsonNode flattenedDocument = jsonFlattener.flatten(document, new DocumentFlattenStrategy());

        int documentSize = flattenedDocument.size(), schemaSize = schema.size();
        if (documentSize > schemaSize) {
            logger.warn("Document structure mismatch: document size={} schema size={}", documentSize, schemaSize);
            return "Wrong document structure";
        }

        boolean allRequired = schemaValidationService.validateRequired(flattenedDocument, schema);
        boolean typesMatch = schemaValidationService.validateType(flattenedDocument, schema);
        boolean uniqueCheck = schemaValidationService.validateUniques(flattenedDocument, schema, collectionPath);

        if (!allRequired) {
            logger.warn("Required fields are missing for document: {}", document);
            return "Required fields are missing!!";
        }
        if (!typesMatch) {
            logger.warn("Types mismatch for document: {}", document);
            return "Types Mismatch";
        }
        if (!uniqueCheck) {
            logger.warn("Unique constraint violation for document: {}", document);
            return "Unique field problem";
        }

        JsonNode finalDocument = addIdField(document);

        HashSet<String> docsNames = JsonFileLister.listJsonFileNames(documentPath);
        String fileName = "doc_" + finalDocument.get("id").asText() + ".json";
        if (docsNames.contains(fileName)) {
            logger.warn("Document with ID {} already exists", finalDocument.get("id").asText());
            return "Document with this ID already Exists";
        }

        JsonNode flattenedFinalDocument = jsonFlattener.flatten(finalDocument, new DocumentFlattenStrategy());

        InsertDocumentEvent insertEvent = new InsertDocumentEvent();
        insertEvent.setUserFolderName(userFolder);
        insertEvent.setDatabaseName(databaseName);
        insertEvent.setCollectionName(collectionName);
        insertEvent.setSchema(schema);
        insertEvent.setFlattenedDocument(flattenedFinalDocument);
        insertEvent.setFinalDocument(finalDocument);
        insertEvent.setDocumentId(finalDocument.get("id").asText());
        kafkaTemplate.send("document-events", insertEvent);

        logger.info("Document inserted successfully with ID: {}", finalDocument.get("id").asText());
        return "Inserted successfully " + insertEvent;
    }

    private JsonNode addIdField(JsonNode document) throws IOException {
        if (!document.has("id")) {
            ObjectNode jsonDoc = (ObjectNode) document;
            jsonDoc.put("id", UUID.randomUUID().toString());
            return jsonDoc;
        }
        return document;
    }

    private String buildUserFolderName(JwtAuthenticationFilter.UserPrincipal user) {
        return user.getUsername() + "_" + user.getId();
    }
}
