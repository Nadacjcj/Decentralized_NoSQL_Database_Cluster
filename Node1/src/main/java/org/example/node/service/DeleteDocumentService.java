package org.example.node.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.node.dto.DeleteDocRequest;
import org.example.node.dto.SchemaRule;
import org.example.node.events.DeleteDocumentEvent;
import org.example.node.filter.JwtAuthenticationFilter;
import org.example.node.repository.JsonRepository;
import org.example.node.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

@Service
public class DeleteDocumentService {

    @Autowired
    private FilteringService filteringService;

    @Autowired
    private JsonRepository jsonRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public DeleteDocumentService() {}

    public String deleteDocument(JwtAuthenticationFilter.UserPrincipal user, String databaseName, String collectionName, DeleteDocRequest deleteDocRequest) throws IOException {
        String userFolder = buildUserFolderName(user);

        Path collectionPath = PathUtil.buildPath(userFolder, databaseName, collectionName);
        Path schemaPath = PathUtil.buildPath(userFolder, databaseName, collectionName).resolve("schema.json");
        JsonNode flattenedSchema = jsonRepository.readIndex(schemaPath);

        try {
            HashSet<String> filteringResults = filteringService.applyFilter(deleteDocRequest.getFilter(), flattenedSchema, collectionPath);
            if(filteringResults.isEmpty()) {
                return "No Documents Found";
            }

            DeleteDocumentEvent deleteEvent = new DeleteDocumentEvent();
            deleteEvent.setUserFolderName(userFolder);
            deleteEvent.setDatabaseName(databaseName);
            deleteEvent.setCollectionName(collectionName);

            deleteEvent.setFlattenedSchema(flattenedSchema);
            deleteEvent.setFilteringResults(filteringResults);
            kafkaTemplate.send("document-events", deleteEvent);

        } catch (Exception e) {
            return "Something went wrong while deleting";
        }

        return "Success";
    }
    private String buildUserFolderName(JwtAuthenticationFilter.UserPrincipal user){
        return user.getUsername() + "_" + user.getId();

    }
}
