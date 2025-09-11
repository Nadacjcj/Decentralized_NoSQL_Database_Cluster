package org.example.node.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.node.dto.UpdateDocRequest;
import org.example.node.dto.SchemaRule;
import org.example.node.repository.JsonRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

@Service
public class IndexUpdaterService {

    private final JsonRepository jsonRepository;

    public IndexUpdaterService(JsonRepository jsonRepository) {
        this.jsonRepository = jsonRepository;
    }

    public void updateIndexes(UpdateDocRequest updateDocRequest,
                              HashSet<String> filteredDocIds,
                              JsonNode flattenedSchema,
                              Path collectionPath,
                              Path documentsPath) throws IOException {

        JsonNode setNode = updateDocRequest.getUpdate().get("set");

        Iterator<Map.Entry<String, JsonNode>> fields = setNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String fieldName = entry.getKey();
            JsonNode newValueNode = entry.getValue();
            String newValue = newValueNode.asText();

            if (!isIndexed(fieldName, flattenedSchema)) continue;

            boolean unique = isUnique(fieldName, flattenedSchema);
            Path indexPath = collectionPath.resolve(toIndexFile(fieldName));

            ObjectNode indexFileContent = jsonRepository.readIndex(indexPath);

            for (String docId : filteredDocIds) {
                ObjectNode docNode = jsonRepository.readIndex(documentsPath.resolve(toDocumentFile(docId)));
                JsonNode oldValueNode = getNestedNode(docNode, fieldName);
                if (oldValueNode == null) continue;

                String oldValue = oldValueNode.asText();
                removeFromIndex(indexFileContent, oldValue, docId, unique);
                addToIndex(indexFileContent, newValue, docId, unique);
            }

            jsonRepository.writeIndex(indexFileContent, indexPath);
        }
    }

    private void removeFromIndex(ObjectNode indexFileContent, String value, String docId, boolean unique) {
        if (unique) {
            indexFileContent.remove(value);
        } else {
            ArrayNode docList = (ArrayNode) indexFileContent.get(value);
            if (docList != null) {
                HashSet<String> docSet = new HashSet<>();
                for (JsonNode idNode : docList) docSet.add(idNode.asText());

                docSet.remove(docId);
                if (docSet.isEmpty()) {
                    indexFileContent.remove(value);
                } else {
                    ArrayNode newArray = indexFileContent.putArray(value);
                    docSet.forEach(newArray::add);
                }
            }
        }
    }

    private void addToIndex(ObjectNode indexFileContent, String value, String docId, boolean unique) {
        if (unique) {
            indexFileContent.put(value, docId);
        } else {
            ArrayNode docList = (ArrayNode) indexFileContent.get(value);
            if (docList == null) docList = indexFileContent.putArray(value);
            docList.add(docId);
        }
    }

    private JsonNode getNestedNode(JsonNode root, String dotNotation) {
        String[] parts = dotNotation.split("\\.");
        JsonNode current = root;
        for (String part : parts) {
            if (current == null) return null;
            current = current.get(part);
        }
        return current;
    }

    private boolean isIndexed(String field, JsonNode flattenedSchema) {
        try {
            SchemaRule schemaRule = new ObjectMapper().treeToValue(flattenedSchema.get(field), SchemaRule.class);
            Boolean indexed = schemaRule.getIndex();
            return indexed != null && indexed;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isUnique(String field, JsonNode flattenedSchema) {
        try {
            SchemaRule schemaRule = new ObjectMapper().treeToValue(flattenedSchema.get(field), SchemaRule.class);
            Boolean unique = schemaRule.isUnique();
            return unique != null && unique;
        } catch (Exception e) {
            return false;
        }
    }

    private String toIndexFile(String fieldName) {
        return fieldName.replace(".", "_DOT_") + "_index.json";
    }

    private String toDocumentFile(String docId) {
        return docId + ".json";
    }
}
