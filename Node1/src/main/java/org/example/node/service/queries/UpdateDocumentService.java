package org.example.node.service.queries;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.node.dto.queries.UpdateDocRequest;
import org.example.node.events.document.UpdateDocumentEvent;
import org.example.node.filter.JwtAuthenticationFilter;
import org.example.node.service.collection.SchemaValidationService;
import org.example.node.util.filesystem.JsonPayloadUtil;
import org.example.node.util.filesystem.PathUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

@Service
public class UpdateDocumentService {
    public static final String SCHEMA_JSON = "schema.json";
    public static final String DOCUMENT_EVENTS = "document-events";
    @Autowired
    private FilteringService filteringService;
    @Autowired
    private SchemaValidationService schemaValidationService;
    private final ObjectMapper mapper;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public UpdateDocumentService(ObjectMapper mapper) {
        this.mapper = mapper;
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true);
    }

    public String updateDocument(JwtAuthenticationFilter.UserPrincipal user,
                                 String databaseName,
                                 String collectionName,
                                 UpdateDocRequest updateDocRequest) {

        String userFolder = buildUserFolderName(user);
        Path collectionPath = PathUtil.buildPath(userFolder, databaseName, collectionName);
        try {
            JsonNode flattenedSchema = JsonPayloadUtil.loadCollectionSchema(collectionPath.resolve(SCHEMA_JSON));

            HashSet<String> filteredDocIds = filteringService.applyFilter(updateDocRequest.getFilter(), flattenedSchema, collectionPath);
            if (!validateUpdateTypes(updateDocRequest, flattenedSchema)) {
                return "Update failed: Type mismatch with schema";
            }

            UpdateDocumentEvent updateEvent = new UpdateDocumentEvent();
            updateEvent.setUserFolderName(userFolder);
            updateEvent.setDatabaseName(databaseName);
            updateEvent.setCollectionName(collectionName);
            updateEvent.setUpdateDocRequest(updateDocRequest);
            updateEvent.setFilteringResults(filteredDocIds);
            updateEvent.setFlattenedSchema(flattenedSchema);
            kafkaTemplate.send(DOCUMENT_EVENTS, updateEvent);

            return "Update Success";
        } catch (Exception e) {
            e.printStackTrace();
            return "Something went wrong while updating";
        }
    }
    private boolean validateUpdateTypes(UpdateDocRequest updateDocRequest, JsonNode flattenedSchema) throws IOException {
        JsonNode updateNode = updateDocRequest.getUpdate();
        JsonNode setNode = updateNode.get("set");

        Iterator<Map.Entry<String, JsonNode>> fields = setNode.fields();
        ObjectNode flattenedUpdate = mapper.createObjectNode();

        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String fieldName = entry.getKey();
            JsonNode newValue = entry.getValue();
            flattenedUpdate.set(fieldName, newValue);
        }

        return schemaValidationService.validateType(flattenedUpdate, flattenedSchema);
    }
    private String buildUserFolderName(JwtAuthenticationFilter.UserPrincipal user){
        return user.getUsername() + "_" + user.getId();

    }
}
