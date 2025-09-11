package org.example.node.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.node.util.BPlusTree;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class BTreeFilterService {

    private BPlusTree indexTree;

    public List<String> performUniqueBTreeFilter(JsonNode indexFileContent, JsonNode condition, String fieldType){
        indexTree = new BPlusTree(3, true, fieldType);
        buildUpUniqueFieldsBTree(indexFileContent);
        return applyOperation(condition, fieldType);
    }

    public List<String> performNonUniqueBTreeFilter(JsonNode indexFileContent, JsonNode condition, String fieldType){
        indexTree = new BPlusTree(3, false, fieldType);
        buildNonUniqueFieldsBTree(indexFileContent);
        return applyOperation(condition, fieldType);
    }

    private void buildUpUniqueFieldsBTree(JsonNode indexJson) {
        indexJson.fields().forEachRemaining(entry -> {
            indexTree.insert(entry.getKey(), entry.getValue().asText());
        });
    }

    private void buildNonUniqueFieldsBTree(JsonNode indexJson) {
        indexJson.fields().forEachRemaining(entry -> {
            entry.getValue().forEach(docIdNode -> indexTree.insert(entry.getKey(), docIdNode.asText()));
        });
    }

    private List<String> applyOperation(JsonNode condition, String fieldType){
        if (condition.has("eq")) {
            return indexTree.search(condition.get("eq").asText());
        } else if (condition.has("ne")) {
            List<String> allDocs = new ArrayList<>(indexTree.rangeQuery(null, true, null, true));
            List<String> exclude = indexTree.search(condition.get("ne").asText());
            if (exclude != null) allDocs.removeAll(exclude);
            return allDocs;
        } else if (condition.has("gt")) {
            return indexTree.rangeQuery(condition.get("gt").asText(), false, null, true);
        } else if (condition.has("lt")) {
            return indexTree.rangeQuery(null, true, condition.get("lt").asText(), false);
        } else if (condition.has("gte")) {
            return indexTree.rangeQuery(condition.get("gte").asText(), true, null, true);
        } else if (condition.has("lte")) {
            return indexTree.rangeQuery(null, true, condition.get("lte").asText(), true);
        } else return new ArrayList<>();
    }
}
