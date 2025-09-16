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
        System.out.println("DEBUG: Update 'set' node = " + setNode);

        Iterator<Map.Entry<String, JsonNode>> fields = setNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String fieldName = entry.getKey();
            JsonNode newValueNode = entry.getValue();
            String newValue = newValueNode.asText();

            boolean indexed = isIndexed(fieldName, flattenedSchema);
            System.out.println("DEBUG: Field '" + fieldName + "' indexed? " + indexed);

            if (!indexed) {
                System.out.println("DEBUG: Skipping field '" + fieldName + "' because it is not indexed");
                continue;
            } else {
                System.out.println("DEBUG: Proceeding with field '" + fieldName + "'");
            }

            boolean unique = isUnique(fieldName, flattenedSchema);
            Path indexPath = collectionPath.resolve(toIndexFile(fieldName));
            System.out.println("DEBUG: Index path = " + indexPath + ", unique = " + unique);

            ObjectNode indexFileContent = jsonRepository.readIndex(indexPath);
            System.out.println("DEBUG: Current index content = " + indexFileContent);

            for (String docId : filteredDocIds) {
                System.out.println("DEBUG: Processing docId = " + docId);

                ObjectNode docNode = jsonRepository.readIndex(documentsPath.resolve(toDocumentFile(docId)));
                if (docNode == null) {
                    System.out.println("WARNING: Document " + docId + " not found at path " + documentsPath.resolve(toDocumentFile(docId)));
                    continue;
                }

                JsonNode oldValueNode = getNestedNode(docNode, fieldName);
                if (oldValueNode == null) {
                    System.out.println("DEBUG: Document " + docId + " has no old value for field " + fieldName);
                    continue;
                }

                String oldValue = oldValueNode.asText();
                System.out.println("DEBUG: Removing old value = " + oldValue + " for docId = " + docId);
                removeFromIndex(indexFileContent, oldValue, docId, unique);

                System.out.println("DEBUG: Adding new value = " + newValue + " for docId = " + docId);
                addToIndex(indexFileContent, newValue, docId, unique);

                System.out.println("DEBUG: Index content after update for docId " + docId + " = " + indexFileContent);
            }

            jsonRepository.writeIndex(indexFileContent, indexPath);
            System.out.println("DEBUG: Written updated index to " + indexPath);
        }
    }

    private void removeFromIndex(ObjectNode indexFileContent, String value, String docId, boolean unique) {
        System.out.println("DEBUG: removeFromIndex called with value=" + value + ", docId=" + docId + ", unique=" + unique);
        if (unique) {
            indexFileContent.remove(value);
            System.out.println("DEBUG: Removed unique value " + value);
        } else {
            ArrayNode docList = (ArrayNode) indexFileContent.get(value);
            if (docList != null) {
                HashSet<String> docSet = new HashSet<>();
                for (JsonNode idNode : docList) docSet.add(idNode.asText());

                docSet.remove(docId);
                if (docSet.isEmpty()) {
                    indexFileContent.remove(value);
                    System.out.println("DEBUG: Removed array value " + value + " because it became empty");
                } else {
                    ArrayNode newArray = indexFileContent.putArray(value);
                    docSet.forEach(newArray::add);
                    System.out.println("DEBUG: Updated array for value " + value + " after removal");
                }
            } else {
                System.out.println("DEBUG: No existing ArrayNode found for value=" + value);
            }
        }
    }

    private void addToIndex(ObjectNode indexFileContent, String value, String docId, boolean unique) {
        System.out.println("DEBUG: addToIndex called with value=" + value + ", docId=" + docId + ", unique=" + unique);
        if (unique) {
            indexFileContent.put(value, docId);
            System.out.println("DEBUG: Added unique value " + value + " -> " + docId);
        } else {
            ArrayNode docList = (ArrayNode) indexFileContent.get(value);
            if (docList == null) docList = indexFileContent.putArray(value);
            docList.add(docId);
            System.out.println("DEBUG: Added to array value " + value + " -> " + docId);
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
            JsonNode fieldNode = flattenedSchema.get(field);
            if (fieldNode != null && fieldNode.has("type")) {
                ((ObjectNode) fieldNode).put("type", fieldNode.get("type").asText().toUpperCase());
            }
            SchemaRule schemaRule = new ObjectMapper().treeToValue(fieldNode, SchemaRule.class);
            Boolean indexed = schemaRule.getIndex();
            return indexed != null && indexed;
        } catch (Exception e) {
            System.out.println("DEBUG: Exception in isIndexed for field=" + field + " -> " + e.getMessage());
            return false;
        }
    }


    private boolean isUnique(String field, JsonNode flattenedSchema) {
        try {
            SchemaRule schemaRule = new ObjectMapper().treeToValue(flattenedSchema.get(field), SchemaRule.class);
            Boolean unique = schemaRule.isUnique();
            return unique != null && unique;
        } catch (Exception e) {
            System.out.println("DEBUG: Exception in isUnique for field=" + field + " -> " + e.getMessage());
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
