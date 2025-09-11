package org.example.node.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.node.dto.SchemaRule;
import org.example.node.repository.JsonRepository;
import org.example.node.util.JsonPayloadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

@Service
public class DocumentDeletionManager {

    @Autowired
    private JsonRepository jsonRepository;

    private final ObjectMapper mapper;

    public DocumentDeletionManager() {
        this.mapper = new ObjectMapper();
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true);
    }

    public void deleteDocuments(HashSet<String> filteredDocsId, JsonNode flattenedSchema, Path collectionPath) throws IOException {
        removeFromIndexes(filteredDocsId, flattenedSchema, collectionPath);
        removeActualDocuments(addFileExtension(filteredDocsId), collectionPath);
    }

    private void removeFromIndexes(HashSet<String> filteredDocsId, JsonNode flattenedSchema, Path collectionPath) throws IOException {
        Iterator<String> fieldNames = flattenedSchema.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();

            if (isIndexed(fieldName, flattenedSchema)) {
                removeDocsFromIndexingFiles(fieldName, filteredDocsId, collectionPath);
            }
        }
    }


    private void removeDocsFromIndexingFiles(String fieldName, HashSet<String> filteredDocsId, Path collectionPath) throws IOException {
        String indexFileName = encodeFileName(fieldName);
        JsonNode indexFileContent = jsonRepository.readIndex(collectionPath.resolve(indexFileName));

        Map<String, JsonNode> indexMap = mapper.convertValue(indexFileContent, new TypeReference<Map<String, JsonNode>>() {});

        Iterator<Map.Entry<String, JsonNode>> iterator = indexMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, JsonNode> entry = iterator.next();
            JsonNode valueNode = entry.getValue();

            if (valueNode.isArray()) {
                List<String> docIds = mapper.convertValue(valueNode, new TypeReference<List<String>>() {});
                docIds.removeIf(filteredDocsId::contains);

                if (docIds.isEmpty()) iterator.remove();
                else entry.setValue(mapper.valueToTree(docIds));

            } else if (valueNode.isTextual()) {
                String docId = valueNode.asText();
                if (filteredDocsId.contains(docId)) iterator.remove();
            } else {
                // keep empty else for unexpected input
            }
        }

        jsonRepository.writeIndex(mapper.valueToTree(indexMap), collectionPath.resolve(indexFileName));
    }

    private void removeActualDocuments(HashSet<String> filteredDocsId , Path collectionPath) throws IOException {
        Path documentsPath = collectionPath.resolve("documents");
        for(String docId : filteredDocsId) {
            JsonPayloadUtil.deleteJsonDoc(documentsPath.resolve(docId));
        }
    }

    private String encodeFileName(String fieldName) throws IOException {
        return fieldName.replace(".", "_DOT_") + "_index.json";
    }

    private boolean isIndexed(String field , JsonNode flattenedSchema) throws JsonProcessingException {
        SchemaRule schemaRule = mapper.treeToValue(flattenedSchema.get(field), SchemaRule.class);
        Boolean indexed = schemaRule.getIndex();
        return indexed != null && indexed;
    }
    private HashSet<String> addFileExtension(HashSet<String> filteredDocsId){
        HashSet<String> plainDocsFilesNames = new HashSet<>();
        for(String docId : filteredDocsId) {
            plainDocsFilesNames.add(docId + ".json");
        }
        return plainDocsFilesNames;
    }
}
