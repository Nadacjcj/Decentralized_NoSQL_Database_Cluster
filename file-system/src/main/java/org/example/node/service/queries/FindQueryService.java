package org.example.node.service.queries;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.node.dto.queries.FindQueryRequest;
import org.example.node.filter.JwtAuthenticationFilter;
import org.example.node.repository.JsonRepository;
import org.example.node.util.filesystem.JsonFileLister;
import org.example.node.util.filesystem.PathUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@Service
public class FindQueryService {

    private static final Logger logger = LogManager.getLogger(FindQueryService.class);

    @Autowired
    private FilteringService filteringService;

    @Autowired
    private JsonRepository jsonRepository;

    public List<JsonNode> find(JwtAuthenticationFilter.UserPrincipal user, String databaseName, String collectionName , FindQueryRequest findQueryRequest) throws IOException {
        Objects.requireNonNull(findQueryRequest, "FindQueryRequest must not be null");
        String userFolder = buildUserFolderName(user);

        Path collectionPath = PathUtil.buildPath(userFolder, databaseName, collectionName);
        Path schemaPath = collectionPath.resolve("schema.json");

        JsonNode flattenedSchema = jsonRepository.readIndex(schemaPath);

        JsonNode filter = findQueryRequest.getFilter();
        if (filter == null || filter.isEmpty()) {
            logger.info("No filter provided, returning all documents for collection: {}", collectionName);
            return findAll(collectionPath);
        }

        logger.info("Applying filter {} on collection {}", filter, collectionName);
        HashSet<String> matchingDocIds = filteringService.applyFilter(filter, flattenedSchema, collectionPath);
        logger.info("Matching document IDs: {}", matchingDocIds);

        return JsonFileLister.listDocsContent(collectionPath, matchingDocIds);
    }

    public List<JsonNode> findAll(Path collectionPath) throws IOException {
        logger.info("Listing all documents in collection path: {}", collectionPath);
        return JsonFileLister.listAllJsonDocsContent(collectionPath);
    }

    private String buildUserFolderName(JwtAuthenticationFilter.UserPrincipal user){
        return user.getUsername() + "_" + user.getId();
    }
}
