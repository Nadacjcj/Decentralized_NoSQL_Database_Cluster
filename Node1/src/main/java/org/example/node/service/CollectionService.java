package org.example.node.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.node.dto.*;
import org.example.node.enums.FieldTypes;
import org.example.node.events.CollectionEvent;
import org.example.node.events.CreateCollectionEvent;
import org.example.node.events.DeleteCollectionEvent;
import org.example.node.events.RenameCollectionEvent;
import org.example.node.filter.JwtAuthenticationFilter;
import org.example.node.repository.JsonRepository;
import org.example.node.util.JsonFlattener;
import org.example.node.util.JsonPayloadUtil;
import org.example.node.util.PathUtil;
import org.example.node.util.SchemaFlattenStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.UUID;

@Service
public class CollectionService {
    @Autowired
    JsonFlattener jsonFlattener;

    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private JsonRepository jsonRepository;
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public String createCollection(JwtAuthenticationFilter.UserPrincipal user, String databaseName, CollectionRequest collectionRequest) throws IOException {

        String userFolder = buildUserFolderName(user);
        CollectionMeta collectionMeta = createCollectionMeta(collectionRequest);
        boolean indexExists = checkKeyExistence(userFolder , databaseName , collectionMeta.getCollectionName() );

        if(indexExists) {
            return "Collection Already Exists";
        }
        if (collectionRequest.getCollectionName() == null || collectionRequest.getCollectionName().isEmpty()) {
            return "Collection name is required";
        }
        if (collectionRequest.getSchema() == null  || collectionRequest.getSchema().isEmpty()) {
            return "Schema name is required";
        }
        CreateCollectionEvent collectionEvent = new CreateCollectionEvent();
        try {
            JsonNode flattenedSchema = createCollectionSchema(
                    userFolder, databaseName,
                    collectionRequest.getCollectionName(),
                    collectionRequest.getSchema()
            );

            collectionEvent.setCollectionRequest(collectionRequest);
            collectionEvent.setCollectionMeta(collectionMeta);
            collectionEvent.setUserFolderName(userFolder);
            collectionEvent.setDatabaseName(databaseName);
            collectionEvent.setFlattenedSchema(flattenedSchema);

            kafkaTemplate.send("collection-events", collectionEvent);

        }
        catch (Exception e){
            return "Folder creation failed for user: " + user.getUsername() + collectionEvent;
        }
        return "Collection created for user: " + user.getUsername() + collectionEvent;
    }

    public String deleteCollection(JwtAuthenticationFilter.UserPrincipal user, String databaseName, CollectionRequest collectionRequest) throws IOException {

        String userFolder = buildUserFolderName(user);
        boolean indexExists = checkKeyExistence(userFolder , databaseName , collectionRequest.getCollectionName() );

        if(!indexExists) {
            return "No Such Collection Exists";
        }
        if (collectionRequest.getCollectionName() == null || collectionRequest.getCollectionName().isEmpty()) {
            return "Folder name is required";
        }
        DeleteCollectionEvent collectionEvent = new DeleteCollectionEvent();
        try {
            collectionEvent.setCollectionRequest(collectionRequest);
            collectionEvent.setUserFolderName(userFolder);
            collectionEvent.setDatabaseName(databaseName);

            kafkaTemplate.send("collection-events", collectionEvent);
            return "Folder deleted for user: " + userFolder + " " + collectionEvent;
        }
        catch (Exception e){
            return "Folder deletion failed for user: " + user.getUsername() + " " + collectionEvent;
        }
    }

    public String renameCollection(JwtAuthenticationFilter.UserPrincipal user, String databaseName , DirectoryRenameRequest request) throws IOException {
        String userFolder = buildUserFolderName(user);
        boolean indexExists = checkKeyExistence(userFolder , databaseName , request.getNewDirectoryName());
        if(indexExists) {
            return "Collection with this name already Exists";
        }

        RenameCollectionEvent collectionEvent = new RenameCollectionEvent();
        try {
            collectionEvent.setRequest(request);
            collectionEvent.setUserFolderName(userFolder);
            collectionEvent.setDatabaseName(databaseName);
            kafkaTemplate.send("collection-events", collectionEvent);
            return "Folder renamed for user: " + userFolder + " " + collectionEvent;
        }
        catch (Exception e){
            return "Folder rename failed for user: " + user.getUsername() + " " + collectionEvent;
        }
    }
    public CollectionMeta createCollectionMeta(CollectionRequest collectionRequest) {
        return new CollectionMeta(
                collectionRequest.getCollectionName() ,
                UUID.randomUUID().toString() ,
                collectionRequest.getSchema());
    }

    public JsonNode createCollectionSchema(String username, String databaseName, String collectionName, JsonNode schema) throws IOException {
        Path filePath = PathUtil.buildPath(username, databaseName, collectionName).resolve("schema.json");
        Files.createDirectories(filePath.getParent());

        File file = filePath.toFile();

        JsonNode flattenedSchema = jsonFlattener.flatten(schema, new SchemaFlattenStrategy());
        JsonNode finalSchema = addIdFieldToSchema(flattenedSchema);
        jsonRepository.writeSchema(file , finalSchema);
        return finalSchema;
    }
    private JsonNode addIdFieldToSchema(JsonNode schema) {
        boolean hasId = false;
        String foundIdKey = null;

        Iterator<String> fieldNames = schema.fieldNames();
        while (fieldNames.hasNext()) {
            String key = fieldNames.next();
            if (key.equalsIgnoreCase("id")) {
                hasId = true;
                foundIdKey = key;
                break;
            }
        }

        ObjectNode jsonDoc = (ObjectNode) schema;

        if (hasId) {
            if (!foundIdKey.equals("id")) {
                JsonNode idValue = jsonDoc.get(foundIdKey);
                jsonDoc.remove(foundIdKey);
                jsonDoc.set("id", idValue);
            }
            return jsonDoc;
        } else {
            JsonNode schemaRuleNode = mapper.valueToTree(
                    new SchemaRule(null, null, FieldTypes.STRING, false)
            );
            jsonDoc.set("id", schemaRuleNode);
            return jsonDoc;
        }

    }
    private String buildUserFolderName(JwtAuthenticationFilter.UserPrincipal user){
        return user.getUsername() + "_" + user.getId();

    }
    public boolean checkKeyExistence(String username , String databaseName , String collectionName) throws IOException {
        return JsonPayloadUtil.loadCollectionIndex(username , databaseName).containsKey(collectionName);
    }

}
