package org.example.node.service.indexing;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.node.util.indexing.BPlusTree;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class BTreeFilterService {

    private BPlusTree indexTree;

    public List<String> performUniqueBTreeFilter(JsonNode indexFileContent, JsonNode condition, String fieldType){
        indexTree = new BPlusTree(3, true, fieldType);
        buildUniqueBTree(indexFileContent);
        return applyOperation(condition);
    }

    public List<String> performNonUniqueBTreeFilter(JsonNode indexFileContent, JsonNode condition, String fieldType){
        indexTree = new BPlusTree(3, false, fieldType);
        buildNonUniqueBTree(indexFileContent);
        return applyOperation(condition);
    }

    private void buildUniqueBTree(JsonNode indexJson) {
        indexJson.fields().forEachRemaining(entry -> {
            indexTree.insert(entry.getKey(), entry.getValue().asText());
        });
    }

    private void buildNonUniqueBTree(JsonNode indexJson) {
        indexJson.fields().forEachRemaining(entry -> {
            entry.getValue().forEach(docIdNode -> indexTree.insert(entry.getKey(), docIdNode.asText()));
        });
    }

    private List<String> applyOperation(JsonNode condition){

        if (condition.has("eq")) {
            return indexTree.search(condition.get("eq").asText());
        } else if (condition.has("ne")) {
            List<String> allDocs = new ArrayList<>(indexTree.rangeQuery(null, true, null, true));
            List<String> exclude = indexTree.search(condition.get("ne").asText());
            if (exclude != null) allDocs.removeAll(exclude);
            return allDocs;
        } else if (condition.has("gt")) {
            return indexTree.rangeQuery(condition.get("gt").asText(), false, null, true);
        } else if (condition.has("gte")) {
            return indexTree.rangeQuery(condition.get("gte").asText(), true, null, true);
        } else if (condition.has("lt")) {
            return indexTree.rangeQuery(null, true, condition.get("lt").asText(), false);
        } else if (condition.has("lte")) {
            return indexTree.rangeQuery(null, true, condition.get("lte").asText(), true);
        } else {
            return new ArrayList<>();
        }
    }
}
