package org.example.node.util.filesystem;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.node.dto.collection.CollectionMeta;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;

public class JsonPayloadUtil {

    private static final Logger logger = LogManager.getLogger(JsonPayloadUtil.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    public static final String DATABASES_INFO_JSON = "databases_info.json";
    public static final String COLLECTIONS_INFO_JSON = "collections_info.json";

    public static void createEmptyJsonFileIfNotExists(File file) throws IOException {
        if (!file.exists()) {
            logger.info("File does not exist. Creating new file: {}", file);
            file.createNewFile();
            mapper.writeValue(file, new HashMap<>());
        }
    }

    public static HashMap<String, Object> loadDatabasesIndex(String username) throws IOException {
        Path filePath = PathUtil.buildPath(username, DATABASES_INFO_JSON);
        File file = filePath.toFile();
        createEmptyJsonFileIfNotExists(file);
        return mapper.readValue(file, HashMap.class);
    }

    public static HashMap<String, CollectionMeta> loadCollectionIndex(String username, String databaseName) throws IOException {
        Path filePath = PathUtil.buildPath(username, databaseName, COLLECTIONS_INFO_JSON);
        File file = filePath.toFile();
        createEmptyJsonFileIfNotExists(file);
        return mapper.readValue(file, HashMap.class);
    }

    public static JsonNode loadCollectionSchema(Path schemaPath) throws IOException {
        File file = schemaPath.toFile();
        if (!file.exists()) {
            logger.warn("Schema file does not exist: {}. Returning empty JSON object.", schemaPath);
            return new ObjectMapper().createObjectNode();
        }
        return mapper.readTree(file);
    }

    public static void deleteJsonDoc(Path docPath) throws IOException {
        File file = docPath.toFile();
        if (!file.delete()) {
            logger.error("Failed to delete file: {}", file.getAbsolutePath());
            throw new IOException("Failed to delete file: " + file.getAbsolutePath());
        }
        logger.info("Deleted JSON document: {}", file.getAbsolutePath());
    }
}
