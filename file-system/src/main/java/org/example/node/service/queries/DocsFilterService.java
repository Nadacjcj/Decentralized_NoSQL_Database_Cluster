package org.example.node.service.queries;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class DocsFilterService {

    private static final Logger logger = LogManager.getLogger(DocsFilterService.class);
    public static final String ID = "id";
    private final ObjectMapper mapper;

    public DocsFilterService(ObjectMapper mapper ) {
        this.mapper = mapper;
    }

    public List<String> filter(List<JsonNode> documents, JsonNode condition) {
        Predicate<JsonNode> predicate = buildPredicate(condition);

        logger.debug("=== DOCS FILTER DEBUG ===");
        logger.debug("Condition: {}", condition);

        return documents.stream()
                .peek(doc -> logger.debug("Checking document: {}", doc))
                .filter(doc -> {
                    boolean match = predicate.test(doc);
                    logger.debug("  -> Match? {}", match);
                    return match;
                })
                .map(doc -> doc.has(ID) ? "doc_" + doc.get(ID).asText() : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Predicate<JsonNode> buildPredicate(JsonNode condition) {
        String field = condition.fieldNames().next();
        JsonNode opNode = condition.get(field);
        String operator = opNode.fieldNames().next();
        JsonNode value = opNode.get(operator);

        return doc -> {
            JsonNode docValue = doc.get(field);

            if (docValue == null) {
                logger.debug("Field missing in document: {}", field);
                return false;
            }

            try {
                if (docValue.isNumber() && value.isNumber()) {
                    double docNum = docValue.asDouble();
                    double condNum = value.asDouble();
                    switch (operator) {
                        case "eq": return docNum == condNum;
                        case "ne": return docNum != condNum;
                        case "gt": return docNum > condNum;
                        case "gte": return docNum >= condNum;
                        case "lt": return docNum < condNum;
                        case "lte": return docNum <= condNum;
                    }
                } else if (docValue.isBoolean() && value.isBoolean()) {
                    boolean docBool = docValue.asBoolean();
                    boolean condBool = value.asBoolean();
                    switch (operator) {
                        case "eq": return docBool == condBool;
                        case "ne": return docBool != condBool;
                        default: return false;
                    }
                } else {
                    String docStr = docValue.asText();
                    String condStr = value.asText();
                    switch (operator) {
                        case "eq": return docStr.equals(condStr);
                        case "ne": return !docStr.equals(condStr);
                        default: return false;
                    }
                }
            } catch (Exception e) {
                logger.error("Error comparing field '{}': {}", field, e.getMessage(), e);
                return false;
            }

            return false;
        };
    }
}
