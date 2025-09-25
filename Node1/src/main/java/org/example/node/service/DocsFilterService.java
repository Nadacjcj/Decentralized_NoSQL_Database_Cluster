package org.example.node.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class DocsFilterService {

    private final ObjectMapper mapper;

    public DocsFilterService() {
        this.mapper = new ObjectMapper();
    }

    public List<String> filter(List<JsonNode> documents, JsonNode condition) {
        Predicate<JsonNode> predicate = buildPredicate(condition);

        return documents.stream()
                .filter(predicate)
                .map(doc -> doc.has("id") ? "doc_" + doc.get("id").asText() : null)
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
            if (docValue == null) return false;

            double docNum, condNum;

            switch (operator) {
                case "eq":
                    return docValue.asText().equals(value.asText());
                case "ne":
                    return !docValue.asText().equals(value.asText());
                case "gt":
                    docNum = docValue.isNumber() ? docValue.asDouble() : Double.parseDouble(docValue.asText());
                    condNum = value.asDouble();
                    return docNum > condNum;
                case "gte":
                    docNum = docValue.isNumber() ? docValue.asDouble() : Double.parseDouble(docValue.asText());
                    condNum = value.asDouble();
                    return docNum >= condNum;
                case "lt":
                    docNum = docValue.isNumber() ? docValue.asDouble() : Double.parseDouble(docValue.asText());
                    condNum = value.asDouble();
                    return docNum < condNum;
                case "lte":
                    docNum = docValue.isNumber() ? docValue.asDouble() : Double.parseDouble(docValue.asText());
                    condNum = value.asDouble();
                    return docNum <= condNum;
                default:
                    throw new IllegalArgumentException("Unknown operator: " + operator);
            }
        };
    }
}
