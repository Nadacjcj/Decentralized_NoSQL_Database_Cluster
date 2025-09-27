package org.example.node.service.queries;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.node.dto.collection.SchemaRule;
import org.example.node.repository.JsonRepository;
import org.example.node.service.indexing.BTreeFilterService;
import org.example.node.service.indexing.JsonIndexingService;
import org.example.node.util.jsonflattener.DocumentFlattenStrategy;
import org.example.node.util.filesystem.JsonFileLister;
import org.example.node.util.jsonflattener.JsonFlattener;
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
            System.out.println("No AND conditions found");
            return null;
        }
        callBtree(flattenedSchema, collectionPath);
        System.out.println("Intersection results: " + resultDocs);
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
                    String[] operators = {"eq", "ne", "gt", "gte", "lt", "lte"};

                    for (String op : operators) {
                        if (condition.has(op)) {
                            ObjectNode node = JsonNodeFactory.instance.objectNode();
                            node.set(fieldName, condition);
                            andListOperations.add(node);
                            System.out.println("Added AND condition: " + node);
                        }
                    }
                }
            }
        }
    }

    public void callBtree(JsonNode flattenedSchema , Path collectionPath) throws JsonProcessingException {

        for (JsonNode node : this.andListOperations) {
            System.out.println("Processing AND node: " + node);

            node.fields().forEachRemaining(entry -> {
                String fieldName = entry.getKey();
                JsonNode condition = entry.getValue();

                try {
                    if(isIndexed(fieldName , flattenedSchema)) {
                        System.out.println("Field is indexed: " + fieldName);
                        String indexFileName = JsonIndexingService.encodeFileName(entry.getKey());
                        JsonNode indexFileContent = jsonRepository.readIndex(collectionPath.resolve(indexFileName));

                        if (isUnique(fieldName, flattenedSchema)) {
                            intersectResults(bTreeFilterService.performUniqueBTreeFilter(indexFileContent, condition , getFieldType(flattenedSchema, fieldName)));
                        } else {
                            intersectResults(bTreeFilterService.performNonUniqueBTreeFilter(indexFileContent, condition , getFieldType(flattenedSchema, fieldName)));
                        }
                    } else {
                        System.out.println("Field NOT indexed: " + fieldName + " -> filtering full docs");
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
        System.out.println("Intersect results: " + btreeResults);

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
        System.out.println("Filtering non-indexed fields with condition: " + condition);
        List<JsonNode> docsContent = JsonFileLister.listAllJsonDocsContent(collectionPath);
        List<JsonNode> flattenedDocsContent =  new ArrayList<>();
        for(JsonNode document : docsContent) {
            flattenedDocsContent.add(jsonFlattener.flatten(document , new DocumentFlattenStrategy()));
        }
        List<String> plainFilteredDocs = docsFilterService.filter(flattenedDocsContent , condition);
        System.out.println("Non-indexed filtered results: " + plainFilteredDocs);
        return plainFilteredDocs;
    }
}
