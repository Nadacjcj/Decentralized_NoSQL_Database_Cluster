package org.example.node.service.indexing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.node.dto.queries.UpdateDocRequest;
import org.example.node.dto.collection.SchemaRule;
import org.example.node.repository.JsonRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Service
public class IndexUpdaterService {

    private static final Logger logger = LogManager.getLogger(IndexUpdaterService.class);

    public static final String TYPE = "type";
    public static final String JSON = ".json";
    public static final String DOT_ = "_DOT_";
    public static final String INDEX_JSON = "_index.json";
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

            boolean indexed = isIndexed(fieldName, flattenedSchema);
            if (!indexed) continue;

            boolean unique = isUnique(fieldName, flattenedSchema);
            Path indexPath = collectionPath.resolve(toIndexFile(fieldName));

            ObjectNode indexFileContent = jsonRepository.readIndex(indexPath);

            for (String docId : filteredDocIds) {

                ObjectNode docNode = jsonRepository.readIndex(documentsPath.resolve(toDocumentFile(docId)));
                if (docNode == null) continue;

                JsonNode oldValueNode = getNestedNode(docNode, fieldName);
                if (oldValueNode == null) continue;

                String oldValue = oldValueNode.asText();
                removeFromIndex(indexFileContent, oldValue, docId, unique);
                addToIndex(indexFileContent, newValue, docId, unique);

                logger.info("Updated index for field '{}' for docId '{}', oldValue='{}', newValue='{}'",
                        fieldName, docId, oldValue, newValue);
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
            } else {
                logger.warn("No existing ArrayNode found for value='{}'", value);
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
            JsonNode fieldNode = flattenedSchema.get(field);
            if (fieldNode != null && fieldNode.has(TYPE)) {
                ((ObjectNode) fieldNode).put(TYPE, fieldNode.get(TYPE).asText().toUpperCase());
            }
            SchemaRule schemaRule = new com.fasterxml.jackson.databind.ObjectMapper().treeToValue(fieldNode, SchemaRule.class);
            Boolean indexed = schemaRule.getIndex();
            return indexed != null && indexed;
        } catch (Exception e) {
            logger.error("Error checking if field '{}' is indexed", field, e);
            return false;
        }
    }

    private boolean isUnique(String field, JsonNode flattenedSchema) {
        try {
            SchemaRule schemaRule = new com.fasterxml.jackson.databind.ObjectMapper().treeToValue(flattenedSchema.get(field), SchemaRule.class);
            Boolean unique = schemaRule.isUnique();
            return unique != null && unique;
        } catch (Exception e) {
            logger.error("Error checking if field '{}' is unique", field, e);
            return false;
        }
    }

    private String toIndexFile(String fieldName) {
        return fieldName.replace(".", DOT_) + INDEX_JSON;
    }

    private String toDocumentFile(String docId) {
        return docId + JSON;
    }
}
