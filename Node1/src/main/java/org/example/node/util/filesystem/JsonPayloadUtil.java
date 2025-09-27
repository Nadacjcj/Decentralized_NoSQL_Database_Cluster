package org.example.node.util.filesystem;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.node.dto.collection.CollectionMeta;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import java.util.HashMap;

public class JsonPayloadUtil {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void createEmptyJsonFileIfNotExists(File file) throws IOException {
        if (!file.exists()) {
            System.out.println("File does not exist it says.. Creating it...");
            file.createNewFile();
            mapper.writeValue(file, new HashMap<>());
        }
    }
    public static HashMap<String , Object> loadDatabasesIndex(String username) throws IOException {
        Path filePath = PathUtil.buildPath(username, "databases_info.json");
        File file = filePath.toFile();
        createEmptyJsonFileIfNotExists(file);
        return mapper.readValue(file, HashMap.class);
    }

    public static HashMap<String , CollectionMeta> loadCollectionIndex(String username , String databaseName) throws IOException {
        Path filePath = PathUtil.buildPath(username , databaseName, "collections_info.json");
        File file = filePath.toFile();
        createEmptyJsonFileIfNotExists(file);
        return mapper.readValue(file, HashMap.class);
    }
    public static JsonNode loadCollectionSchema(Path schemaPath) throws IOException {
        File file = schemaPath.toFile();
        if (!file.exists()) {
            return new ObjectMapper().createObjectNode();
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(file);

    }
    public static void deleteJsonDoc(Path docPath) throws IOException {
        File file = docPath.toFile();
        if (!file.delete()) {
            throw new IOException("Failed to delete file: " + file.getAbsolutePath());
        }
    }

}
