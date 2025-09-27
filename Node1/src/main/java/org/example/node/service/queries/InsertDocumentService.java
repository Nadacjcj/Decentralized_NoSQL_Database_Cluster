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
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.UUID;

@Service
public class InsertDocumentService {

    @Autowired
    private JsonFlattener jsonFlattener;
    @Autowired
    private SchemaValidationService schemaValidationService;
    @Autowired
    private JsonRepository jsonRepository;
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    private final ObjectMapper mapper;

    public InsertDocumentService(ObjectMapper mapper ) {
        this.mapper = mapper;
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true);
    }

    public String insertDocument(JwtAuthenticationFilter.UserPrincipal user, String databaseName, String collectionName , JsonNode document) throws IOException {
        String userFolder = buildUserFolderName(user);

        Path collectionPath = PathUtil.buildPath(userFolder, databaseName, collectionName);
        Path documentPath = PathUtil.buildPath(userFolder, databaseName, collectionName).resolve("documents");
        Path schemaPath = PathUtil.buildPath(userFolder, databaseName, collectionName).resolve("schema.json");

        JsonNode schema = JsonRepository.readIndex(schemaPath);
        JsonNode flattenedDocument = jsonFlattener.flatten(document , new DocumentFlattenStrategy());

        int documentSize = flattenedDocument.size() , schemaSize = schema.size();
        if(documentSize > schemaSize){
            return "Wrong document structure ";
        }

        boolean allRequired = schemaValidationService.validateRequired(flattenedDocument, schema);
        boolean typesMatch = schemaValidationService.validateType(flattenedDocument, schema);
        boolean uniqueCheck = schemaValidationService.validateUniques(flattenedDocument , schema , collectionPath);

        // I prefer if an exception is returned with a specific message
        if(!allRequired){
            return "Required fields are missing!!";
        }
        if(!typesMatch){
            return "Types Mismatch";
        }
        if(!uniqueCheck){
            return "Unique field problem";
        }

        JsonNode finalDocument = addIdField(document);

        HashSet<String> docsNames = JsonFileLister.listJsonFileNames(documentPath);
        String fileName = "doc_" + document.get("id").asText() + ".json";
        if(docsNames.contains(fileName)){
            return "Document with this ID already Exists";
        }
        JsonNode flattenedFinalDocument = jsonFlattener.flatten(finalDocument , new  DocumentFlattenStrategy());

        InsertDocumentEvent insertEvent = new InsertDocumentEvent();
        insertEvent.setUserFolderName(userFolder);
        insertEvent.setDatabaseName(databaseName);
        insertEvent.setCollectionName(collectionName);
        insertEvent.setSchema(schema);
        insertEvent.setFlattenedDocument(flattenedFinalDocument);
        insertEvent.setFinalDocument(finalDocument);
        insertEvent.setDocumentId(finalDocument.get("id").asText());
        kafkaTemplate.send("document-events", insertEvent);

        return "Inserted successfully " + insertEvent;
    }

    private JsonNode addIdField(JsonNode document) throws IOException {
        if(!document.has("id")){
            ObjectNode jsonDoc = (ObjectNode)document;
            jsonDoc.put("id", UUID.randomUUID().toString());
            return jsonDoc;
        }
        return document;
    }
    private String buildUserFolderName(JwtAuthenticationFilter.UserPrincipal user){
        return user.getUsername() + "_" + user.getId();

    }


}
