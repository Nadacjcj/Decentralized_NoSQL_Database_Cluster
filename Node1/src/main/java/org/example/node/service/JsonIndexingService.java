package org.example.node.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.node.dto.SchemaRule;
import org.example.node.repository.JsonRepository;
import org.example.node.util.JsonPayloadUtil;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;

@Service
public class JsonIndexingService {
    private final ObjectMapper mapper = new ObjectMapper();
    private final JsonRepository jsonRepository;

    public JsonIndexingService(JsonRepository jsonRepository) {
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true);
        this.jsonRepository = jsonRepository;
    }
    public void checkIndexableFields(JsonNode schema, Path collectionPath) {
        Iterator<String> fieldNames = schema.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            JsonNode fieldValue = schema.get(fieldName);

            try {
                SchemaRule schemaRule = mapper.treeToValue(fieldValue, SchemaRule.class);
                if (Boolean.TRUE.equals(schemaRule.getIndex())) {
                    createIndexFile(fieldName , collectionPath);
                }
            } catch (JsonProcessingException e) {
                System.err.println("JSON parsing error for field: " + fieldName);
            } catch (IOException e) {
                System.err.println("IO error for field: " + fieldName);
            }
        }
    }
    public void createIndexFile(String plainFieldName , Path collectionPath) throws IOException {
        String indexFileName = encodeFileName(plainFieldName);
        Path indexFilePath = collectionPath.resolve(indexFileName);
        File infexFile = indexFilePath.toFile();
        JsonPayloadUtil.createEmptyJsonFileIfNotExists(infexFile);
    }

    public static String encodeFileName(String fieldName) throws IOException {
        return fieldName.replace(".", "_DOT_") + "_index.json";
    }

    public void updateIndexes(JsonNode schema, JsonNode document, Path collectionPath) throws IOException {
        Iterator<String> fieldNames = document.fieldNames();
        while (fieldNames.hasNext()) {
            String key = fieldNames.next();

            JsonNode schemaValueNode = schema.get(key);
            SchemaRule schemaRule = mapper.treeToValue(schemaValueNode, SchemaRule.class);

            if(schemaRule.getIndex() == Boolean.TRUE) {
                if(schemaRule.isUnique() == Boolean.TRUE) {
                    updateUniqueFieldsIndex(key , document , collectionPath);
                }else{
                    updateNonUniqueFiled(key , document , collectionPath);
                }
            }

        }
    }
    void updateUniqueFieldsIndex(String fieldName , JsonNode document, Path collectionPath) throws IOException {
        ObjectNode loadedIndex = jsonRepository.readIndex(collectionPath.resolve(encodeFileName(fieldName)));
        String docIdValue = "doc_" + document.get("id").asText();
        loadedIndex.put(document.get(fieldName).asText(), docIdValue);
        jsonRepository.writeIndex(loadedIndex, collectionPath.resolve(encodeFileName(fieldName)));
    }
    void updateNonUniqueFiled(String fieldName , JsonNode document, Path collectionPath) throws IOException {
        ObjectNode loadedIndex = jsonRepository.readIndex(collectionPath.resolve(encodeFileName(fieldName)));

        ArrayNode docsArray;
        String fieldValue = document.get(fieldName).asText();

        if (loadedIndex.has(fieldValue)) {
            docsArray = (ArrayNode) loadedIndex.get(fieldValue);
        }else {
            docsArray = mapper.createArrayNode();
            loadedIndex.set(fieldValue, docsArray);
        }


        String storedDocName = "doc_" + document.get("id").asText();
        boolean exists = false;
        for (JsonNode n : docsArray) {
            if (n.asText().equals(storedDocName)) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            docsArray.add(storedDocName);
        }
        jsonRepository.writeIndex(loadedIndex, collectionPath.resolve(encodeFileName(fieldName)));
        System.out.println("Updating non-unique fields index");
    }

}
