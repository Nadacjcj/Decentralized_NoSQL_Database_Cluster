package org.example.node.service.queries;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.node.dto.queries.UpdateDocRequest;
import org.example.node.repository.JsonRepository;
import org.springframework.stereotype.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

@Service
public class DocumentUpdaterService {

    private static final Logger logger = LogManager.getLogger(DocumentUpdaterService.class);
    public static final String JSON = ".json";
    private final JsonRepository jsonRepository;

    public DocumentUpdaterService(JsonRepository jsonRepository) {
        this.jsonRepository = jsonRepository;
    }

    public void updateDocuments(UpdateDocRequest updateDocRequest,
                                HashSet<String> filteredDocIds,
                                Path documentsPath) throws IOException {

        JsonNode setNode = updateDocRequest.getUpdate().get("set");

        for (String docId : filteredDocIds) {
            ObjectNode docNode = jsonRepository.readIndex(documentsPath.resolve(toDocumentFile(docId)));
            logger.debug("Updating document: {}", docId);

            Iterator<Map.Entry<String, JsonNode>> fields = setNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String fieldName = entry.getKey();
                setNestedValue(docNode, fieldName, entry.getValue());
                logger.debug("Updated field '{}' to value '{}'", fieldName, entry.getValue());
            }

            jsonRepository.writeIndex(docNode, documentsPath.resolve(toDocumentFile(docId)));
        }
    }

    private void setNestedValue(ObjectNode root, String dotNotation, JsonNode newValue) {
        String[] parts = dotNotation.split("\\.");
        ObjectNode current = root;

        for (int i = 0; i < parts.length - 1; i++) {
            JsonNode child = current.get(parts[i]);
            if (child == null || !child.isObject()) {
                ObjectNode newObj = current.objectNode();
                current.set(parts[i], newObj);
                current = newObj;
            } else {
                current = (ObjectNode) child;
            }
        }

        current.set(parts[parts.length - 1], newValue);
    }

    private String toDocumentFile(String docId) {
        return docId + JSON;
    }
}
