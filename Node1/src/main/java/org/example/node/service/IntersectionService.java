package org.example.node.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.node.dto.SchemaRule;
import org.example.node.repository.JsonRepository;
import org.example.node.util.DocumentFlattenStrategy;
import org.example.node.util.JsonFileLister;
import org.example.node.util.JsonFlattener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

@Service
public class IntersectionService {

    @Autowired
    private JsonRepository jsonRepository;
    @Autowired
    private BTreeFilterService bTreeFilterService;
    @Autowired
    private JsonFlattener jsonFlattener;
    @Autowired
    private DocsFilterService docsFilterService;

    private List<JsonNode> andListOperations;
    private final ObjectMapper mapper;
    private HashSet<String> resultDocs;

    public IntersectionService() {
        this.mapper  = new ObjectMapper();
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true);
    }
    public HashSet<String> performIntersection(JsonNode filter , JsonNode flattenedSchema , Path collectionPath) throws JsonProcessingException {
        this.andListOperations = new ArrayList<>();
        this.resultDocs = new HashSet<>();
        traverseAndConditions(filter, flattenedSchema);

        if(this.andListOperations.isEmpty()) {
            return null;
        }
        callBtree(flattenedSchema, collectionPath);
        return resultDocs;
    }

    private void traverseAndConditions(JsonNode filter, JsonNode flattenedSchema) throws JsonProcessingException {
        if (filter.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = filter.fields();

            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String fieldName = entry.getKey();
                JsonNode condition = entry.getValue();

                if (condition.isObject() && flattenedSchema.has(fieldName)) {
                    String[] operators = {"eq", "ne", "gt", "gte", "lt", "lte"}; // constants (I prefer ENUMs)

                    for (String op : operators) {
                        if (condition.has(op)) {

                            ObjectNode node = JsonNodeFactory.instance.objectNode();
                            node.set(fieldName, condition);
                            andListOperations.add(node);
                        }
                    }
                }
            }
        }
    }

    public void callBtree(JsonNode flattenedSchema , Path collectionPath) throws JsonProcessingException {

        for (JsonNode node : this.andListOperations) {

            node.fields().forEachRemaining(entry -> {
                String fieldName = entry.getKey(); // e.g., "address.city"
                JsonNode condition = entry.getValue(); // e.g., {"eq":"Irbid"}

                try {
                    if(isIndexed(fieldName , flattenedSchema)) {
                        String indexFileName = JsonIndexingService.encodeFileName(entry.getKey());
                        JsonNode indexFileContent = jsonRepository.readIndex(collectionPath.resolve(indexFileName));

                        if (isUnique(fieldName, flattenedSchema)) {
                            intersectResults(bTreeFilterService.performUniqueBTreeFilter(indexFileContent, condition , getFieldType(flattenedSchema, fieldName)));
                        } else {
                            intersectResults(bTreeFilterService.performNonUniqueBTreeFilter(indexFileContent, condition , getFieldType(flattenedSchema, fieldName)));
                        }
                    }else {
                        ObjectNode fullCondition = mapper.createObjectNode();
                        fullCondition.set(fieldName, condition);
                        intersectResults(filterNonIndexedFields(fullCondition, collectionPath));
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
    private boolean isUnique(String field , JsonNode flattenedSchema) throws JsonProcessingException {
        SchemaRule schemaRule = mapper.treeToValue(flattenedSchema.get(field), SchemaRule.class);
        Boolean unique = schemaRule.isUnique();
        return unique != null && unique;
    }

    private boolean isIndexed(String field , JsonNode flattenedSchema) throws JsonProcessingException {
        SchemaRule schemaRule = mapper.treeToValue(flattenedSchema.get(field), SchemaRule.class);
        Boolean indexed = schemaRule.getIndex();
        return indexed != null && indexed;
    }
    private void intersectResults(List<String> btreeResults) throws JsonProcessingException {
        System.out.println("BTREEEEEEEEE Intersect RESULTS HEREEE  " + btreeResults);

        if (resultDocs.isEmpty()) {
            resultDocs.addAll(btreeResults);
        } else {
            resultDocs.retainAll(btreeResults);
        }
    }
    private String getFieldType(JsonNode flattenedSchema , String fieldName) throws JsonProcessingException {
        SchemaRule schemaRule = mapper.treeToValue(flattenedSchema.get(fieldName), SchemaRule.class);
        return schemaRule.getType().toString().toLowerCase();

    }
    private List<String> filterNonIndexedFields(JsonNode condition  , Path collectionPath) throws IOException {
        List<JsonNode> docsContent = JsonFileLister.listAllJsonDocsContent(collectionPath);
        List<JsonNode> flattenedDocsContent =  new ArrayList<>();
        for(JsonNode document : docsContent) {
            flattenedDocsContent.add(jsonFlattener.flatten(document , new DocumentFlattenStrategy()));
        }
        List<String> plainFilteredDocs = docsFilterService.filter(flattenedDocsContent , condition);
        return plainFilteredDocs;
    }

}
