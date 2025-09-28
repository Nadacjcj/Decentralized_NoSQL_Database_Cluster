package org.example.node.service.indexing;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.node.util.indexing.BPlusTree;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Service
public class BTreeFilterService {

    private static final Logger logger = LogManager.getLogger(BTreeFilterService.class);

    private BPlusTree indexTree;

    public List<String> performUniqueBTreeFilter(JsonNode indexFileContent, JsonNode condition, String fieldType) {
        indexTree = new BPlusTree(3, true, fieldType);
        buildUniqueBTree(indexFileContent);
        logger.info("Built unique B-Tree for fieldType '{}'", fieldType);
        return applyOperation(condition);
    }

    public List<String> performNonUniqueBTreeFilter(JsonNode indexFileContent, JsonNode condition, String fieldType) {
        indexTree = new BPlusTree(3, false, fieldType);
        buildNonUniqueBTree(indexFileContent);
        logger.info("Built non-unique B-Tree for fieldType '{}'", fieldType);
        return applyOperation(condition);
    }

    private void buildUniqueBTree(JsonNode indexJson) {
        indexJson.fields().forEachRemaining(entry -> indexTree.insert(entry.getKey(), entry.getValue().asText()));
    }

    private void buildNonUniqueBTree(JsonNode indexJson) {
        indexJson.fields().forEachRemaining(entry ->
                entry.getValue().forEach(docIdNode -> indexTree.insert(entry.getKey(), docIdNode.asText()))
        );
    }

    private List<String> applyOperation(JsonNode condition) {
        List<String> result;
        if (condition.has("eq")) {
            result = indexTree.search(condition.get("eq").asText());
            logger.info("Applied 'eq' filter with value '{}', found {} results", condition.get("eq").asText(), result.size());
        } else if (condition.has("ne")) {
            List<String> allDocs = new ArrayList<>(indexTree.rangeQuery(null, true, null, true));
            List<String> exclude = indexTree.search(condition.get("ne").asText());
            if (exclude != null) allDocs.removeAll(exclude);
            result = allDocs;
            logger.info("Applied 'ne' filter with value '{}', remaining {} results", condition.get("ne").asText(), result.size());
        } else if (condition.has("gt")) {
            result = indexTree.rangeQuery(condition.get("gt").asText(), false, null, true);
            logger.info("Applied 'gt' filter with value '{}', found {} results", condition.get("gt").asText(), result.size());
        } else if (condition.has("gte")) {
            result = indexTree.rangeQuery(condition.get("gte").asText(), true, null, true);
            logger.info("Applied 'gte' filter with value '{}', found {} results", condition.get("gte").asText(), result.size());
        } else if (condition.has("lt")) {
            result = indexTree.rangeQuery(null, true, condition.get("lt").asText(), false);
            logger.info("Applied 'lt' filter with value '{}', found {} results", condition.get("lt").asText(), result.size());
        } else if (condition.has("lte")) {
            result = indexTree.rangeQuery(null, true, condition.get("lte").asText(), true);
            logger.info("Applied 'lte' filter with value '{}', found {} results", condition.get("lte").asText(), result.size());
        } else {
            result = new ArrayList<>();
            logger.warn("No valid filter found in condition: {}", condition);
        }
        return result;
    }
}
