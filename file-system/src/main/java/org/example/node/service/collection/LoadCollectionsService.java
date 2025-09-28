package org.example.node.service.collection;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.node.filter.JwtAuthenticationFilter;
import org.example.node.repository.JsonRepository;
import org.example.node.util.filesystem.PathUtil;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Service
public class LoadCollectionsService {

    private static final Logger logger = LogManager.getLogger(LoadCollectionsService.class);

    public static final String DATABASE_NAME = "databaseName";
    public static final String COLLECTIONS_INFO_JSON = "collections_info.json";
    public static final String SCHEMA_JSON = "schema.json";

    public JsonNode loadCollections(JwtAuthenticationFilter.UserPrincipal user, JsonNode DBName) throws IOException {
        String userFolderName = buildDBFolderName(user);
        String databaseName = DBName.get(DATABASE_NAME).asText();

        Path userFolderPath = PathUtil.buildPath(userFolderName, databaseName, COLLECTIONS_INFO_JSON);
        JsonNode result = JsonRepository.readDoc(userFolderPath);
        logger.info("Loaded collections info from path {}", userFolderPath);
        return result;
    }

    public JsonNode loadSchema(JwtAuthenticationFilter.UserPrincipal user, String databaseName, String collectionName) throws IOException {
        String userFolderName = buildDBFolderName(user);
        Path userFolderPath = PathUtil.buildPath(userFolderName, databaseName, collectionName).resolve(SCHEMA_JSON);
        JsonNode result = JsonRepository.readDoc(userFolderPath);
        logger.info("Loaded schema for collection '{}' from path {}", collectionName, userFolderPath);
        return result;
    }

    private String buildDBFolderName(JwtAuthenticationFilter.UserPrincipal user) {
        return user.getUsername() + "_" + user.getId();
    }
}
