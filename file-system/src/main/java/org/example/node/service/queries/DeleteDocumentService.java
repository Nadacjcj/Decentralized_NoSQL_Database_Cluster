package org.example.node.service.queries;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.node.dto.queries.DeleteDocRequest;
import org.example.node.events.document.DeleteDocumentEvent;
import org.example.node.filter.JwtAuthenticationFilter;
import org.example.node.repository.JsonRepository;
import org.example.node.util.filesystem.PathUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;

@Service
public class DeleteDocumentService {

    private static final Logger logger = LogManager.getLogger(DeleteDocumentService.class);

    public static final String SCHEMA_JSON = "schema.json";
    public static final String NO_DOCUMENTS_FOUND = "No Documents Found";
    public static final String DOCUMENT_EVENTS = "document-events";
    public static final String SOMETHING_WENT_WRONG_WHILE_DELETING = "Something went wrong while deleting";
    public static final String SUCCESS = "Success";

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
        Path schemaPath = PathUtil.buildPath(userFolder, databaseName, collectionName).resolve(SCHEMA_JSON);
        JsonNode flattenedSchema = jsonRepository.readIndex(schemaPath);

        try {
            HashSet<String> filteringResults = filteringService.applyFilter(deleteDocRequest.getFilter(), flattenedSchema, collectionPath);
            if(filteringResults.isEmpty()) {
                logger.info(NO_DOCUMENTS_FOUND);
                return NO_DOCUMENTS_FOUND;
            }

            DeleteDocumentEvent deleteEvent = new DeleteDocumentEvent();
            deleteEvent.setUserFolderName(userFolder);
            deleteEvent.setDatabaseName(databaseName);
            deleteEvent.setCollectionName(collectionName);
            deleteEvent.setFlattenedSchema(flattenedSchema);
            deleteEvent.setFilteringResults(filteringResults);

            kafkaTemplate.send(DOCUMENT_EVENTS, deleteEvent);
            logger.info("Delete event sent for documents: {}", filteringResults);

        } catch (Exception e) {
            logger.error(SOMETHING_WENT_WRONG_WHILE_DELETING, e);
            return SOMETHING_WENT_WRONG_WHILE_DELETING;
        }

        return SUCCESS;
    }

    private String buildUserFolderName(JwtAuthenticationFilter.UserPrincipal user){
        return user.getUsername() + "_" + user.getId();
    }
}
