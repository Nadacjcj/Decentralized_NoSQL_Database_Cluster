package org.example.node.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.node.dto.*;
import org.example.node.enums.FieldTypes;
import org.example.node.events.CreateCollectionEvent;
import org.example.node.events.DeleteCollectionEvent;
import org.example.node.events.RenameCollectionEvent;
import org.example.node.filter.JwtAuthenticationFilter;
import org.example.node.repository.JsonRepository;
import org.example.node.util.JsonFlattener;
import org.example.node.util.JsonPayloadUtil;
import org.example.node.util.PathUtil;
import org.example.node.util.SchemaFlattenStrategy;
import org.example.node.locks.ConsulLockService;
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

    @Autowired private JsonFlattener jsonFlattener;
    @Autowired private JsonRepository jsonRepository;
    @Autowired private KafkaTemplate<String, Object> kafkaTemplate;
    @Autowired private ConsulLockService lockService;

    @Autowired CollectionManagementService collectionManagementService;
    @Autowired CollectionIndexService collectionIndexService;
    @Autowired JsonIndexingService jsonIndexingService;
    private final ObjectMapper mapper = new ObjectMapper();

    public String createCollection(JwtAuthenticationFilter.UserPrincipal user,
                                   String databaseName,
                                   CollectionRequest collectionRequest) throws IOException {

        String userFolder = buildUserFolderName(user);
        CollectionMeta collectionMeta = createCollectionMeta(collectionRequest);

        String validation = validateCreateRequest(userFolder, databaseName, collectionRequest);
        if (validation != null) return validation;

        String lockKey = generateLockKey(userFolder, databaseName, collectionRequest.getCollectionName());

        return withDistributedLock(lockKey, () -> {
            JsonNode flattenedSchema = createCollectionSchema(userFolder, databaseName,
                    collectionRequest.getCollectionName(), collectionRequest.getSchema());
            CreateCollectionEvent event = buildCreateEvent(userFolder, databaseName,
                    collectionRequest, collectionMeta, flattenedSchema);

            event.process(collectionManagementService, collectionIndexService, jsonIndexingService);
            kafkaTemplate.send("collection-events", event);

            return "Collection created for user: " + user.getUsername() + " " + event;
        });

    }

    public String deleteCollection(JwtAuthenticationFilter.UserPrincipal user,
                                   String databaseName,
                                   CollectionRequest collectionRequest) throws IOException {

        String userFolder = buildUserFolderName(user);

        String validation = validateDeleteRequest(userFolder, databaseName, collectionRequest);
        if (validation != null) return validation;

        String lockKey = generateLockKey(userFolder, databaseName, collectionRequest.getCollectionName());

        return withDistributedLock(lockKey, () -> {
            // === APPLY LOCAL CHANGES FIRST ===
            DeleteCollectionEvent event = new DeleteCollectionEvent();
            event.setCollectionRequest(collectionRequest);
            event.setUserFolderName(userFolder);
            event.setDatabaseName(databaseName);
            event.process(collectionManagementService, collectionIndexService, jsonIndexingService);

            // === PUBLISH EVENT TO OTHER NODES ===
            kafkaTemplate.send("collection-events", event);

            return "Folder deleted for user: " + userFolder + " " + event;
        });
    }

    public String renameCollection(JwtAuthenticationFilter.UserPrincipal user,
                                   String databaseName,
                                   DirectoryRenameRequest request) throws IOException {

        String userFolder = buildUserFolderName(user);

        if (checkKeyExistence(userFolder, databaseName, request.getNewDirectoryName()))
            return "Collection with this name already Exists";

        String lockKey = generateLockKey(userFolder, databaseName, request.getOldDirectoryName());

        return withDistributedLock(lockKey, () -> {
            // === APPLY LOCAL CHANGES FIRST ===
            RenameCollectionEvent event = new RenameCollectionEvent();
            event.setRequest(request);
            event.setUserFolderName(userFolder);
            event.setDatabaseName(databaseName);
            event.process(collectionManagementService, collectionIndexService, jsonIndexingService);

            // === PUBLISH EVENT TO OTHER NODES ===
            kafkaTemplate.send("collection-events", event);

            return "Folder renamed for user: " + userFolder + " " + event;
        });
    }

    public CollectionMeta createCollectionMeta(CollectionRequest collectionRequest) {
        return new CollectionMeta(
                collectionRequest.getCollectionName(),
                UUID.randomUUID().toString(),
                collectionRequest.getSchema()
        );
    }

    public JsonNode createCollectionSchema(String username,
                                           String databaseName,
                                           String collectionName,
                                           JsonNode schema) throws IOException {

        Path filePath = PathUtil.buildPath(username, databaseName, collectionName).resolve("schema.json");
        Files.createDirectories(filePath.getParent());

        File file = filePath.toFile();
        JsonNode flattenedSchema = jsonFlattener.flatten(schema, new SchemaFlattenStrategy());
        JsonNode finalSchema = addIdFieldToSchema(flattenedSchema);

        jsonRepository.writeSchema(file, finalSchema);
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
        } else {
            JsonNode schemaRuleNode = mapper.valueToTree(new SchemaRule(null, null, FieldTypes.STRING, false));
            jsonDoc.set("id", schemaRuleNode);
        }
        return jsonDoc;
    }

    private String buildUserFolderName(JwtAuthenticationFilter.UserPrincipal user) {
        return user.getUsername() + "_" + user.getId();
    }

    private boolean checkKeyExistence(String username, String databaseName, String collectionName) throws IOException {
        return JsonPayloadUtil.loadCollectionIndex(username, databaseName).containsKey(collectionName);
    }

    private String generateLockKey(String userFolder, String databaseName, String collectionName) {
        return "collection:" + userFolder + ":" + databaseName + ":" + collectionName;
    }

    private String withDistributedLock(String lockKey, LockCallback callback) throws IOException {
        boolean lockAcquired = lockService.tryAcquireWithRetry(() -> lockService.acquireWriteLock(lockKey), 10);
        if (!lockAcquired) throw new IllegalStateException("Could not acquire lock for " + lockKey);

        try {
            return callback.execute();
        } finally {
            lockService.releaseWriteLock(lockKey);
        }
    }

    private String validateCreateRequest(String userFolder, String databaseName, CollectionRequest collectionRequest) throws IOException {
        if (collectionRequest.getCollectionName() == null || collectionRequest.getCollectionName().isEmpty())
            return "Collection name is required";
        if (collectionRequest.getSchema() == null || collectionRequest.getSchema().isEmpty())
            return "Schema name is required";
        if (checkKeyExistence(userFolder, databaseName, collectionRequest.getCollectionName()))
            return "Collection Already Exists";
        return null;
    }

    private String validateDeleteRequest(String userFolder, String databaseName, CollectionRequest collectionRequest) throws IOException {
        if (collectionRequest.getCollectionName() == null || collectionRequest.getCollectionName().isEmpty())
            return "Folder name is required";
        if (!checkKeyExistence(userFolder, databaseName, collectionRequest.getCollectionName()))
            return "No Such Collection Exists";
        return null;
    }

    private CreateCollectionEvent buildCreateEvent(String userFolder, String databaseName,
                                                   CollectionRequest request, CollectionMeta meta, JsonNode flattenedSchema) {
        CreateCollectionEvent event = new CreateCollectionEvent();
        event.setCollectionRequest(request);
        event.setCollectionMeta(meta);
        event.setUserFolderName(userFolder);
        event.setDatabaseName(databaseName);
        event.setFlattenedSchema(flattenedSchema);
        return event;
    }

    @FunctionalInterface
    private interface LockCallback {
        String execute() throws IOException;
    }
}
