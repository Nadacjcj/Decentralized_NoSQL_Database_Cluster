package org.example.node.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.node.dto.FolderMeta;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;

@Repository
public class JsonRepository {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static ObjectNode readIndex(Path jsonFilePath) throws IOException {
        try {
            return objectMapper.readValue(jsonFilePath.toFile(), new TypeReference<ObjectNode>() {});
        } catch (IOException e) {
            return objectMapper.createObjectNode();
        }
    }

    public void writeIndex(ObjectNode dbMap, Path jsonFilePath) throws IOException {
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(jsonFilePath.toFile(), dbMap);
    }
    public void writeSchema(File schemaFile , JsonNode flattenedSchema) throws IOException {
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(schemaFile, flattenedSchema);

    }
    public static ObjectNode readDoc(Path jsonFilePath) throws IOException {
        try {
            return objectMapper.readValue(jsonFilePath.toFile(), new TypeReference<ObjectNode>() {});
        } catch (IOException e) {
            return objectMapper.createObjectNode();
        }
    }
}
