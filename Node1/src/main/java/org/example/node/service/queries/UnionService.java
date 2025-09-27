package org.example.node.service.queries;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
public class UnionService {

    @Autowired
    private JsonRepository jsonRepository;
    @Autowired
    private BTreeFilterService bTreeFilterService;
    @Autowired
    private DocsFilterService docsFilterService;
    @Autowired
    private JsonFlattener jsonFlattener;

    private List<JsonNode> orListOperations;
    private final ObjectMapper mapper;
    private HashSet<String> resultDocs;

    public UnionService() {
        this.mapper  = new ObjectMapper();
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true);
    }

    public HashSet<String> performUnion(JsonNode filter, JsonNode flattenedSchema , Path collectionPath) throws JsonProcessingException {
        orListOperations = new ArrayList<>();
        resultDocs = new HashSet<>();

        traverseOrConditions(filter);
        if(this.orListOperations.isEmpty()) {
            System.out.println("No OR conditions found");
            return null;
        }
        callBtree(flattenedSchema , collectionPath);
        System.out.println("Union results: " + resultDocs);
        return resultDocs;
    }

    public void traverseOrConditions(JsonNode filter) {
        if (filter.isObject()) {
            if (filter.has("or")) {
                for (JsonNode subFilter : filter.get("or")) {
                    orListOperations.add(subFilter);
                    System.out.println("Added OR condition: " + subFilter);
                }
            }
            filter.fields().forEachRemaining(entry -> {
                JsonNode value = entry.getValue();
                if (value.isObject() || value.isArray()) {
                    traverseOrConditions(value);
                }
            });
        } else if (filter.isArray()) {
            for (JsonNode element : filter) {
                traverseOrConditions(element);
            }
        }
    }

    public void callBtree(JsonNode flattenedSchema , Path collectionPath) throws JsonProcessingException {
        for (JsonNode node : this.orListOperations) {
            System.out.println("Processing OR node: " + node);

            node.fields().forEachRemaining(entry -> {
                String fieldName = entry.getKey();
                JsonNode condition = entry.getValue();

                try {
                    if(isIndexed(fieldName , flattenedSchema)) {
                        System.out.println("Field is indexed: " + fieldName);
                        String indexFileName = JsonIndexingService.encodeFileName(entry.getKey());
                        JsonNode indexFileContent = jsonRepository.readIndex(collectionPath.resolve(indexFileName));

                        if (isUnique(fieldName, flattenedSchema)) {
                            joinResults(bTreeFilterService.performUniqueBTreeFilter(indexFileContent,condition, getFieldType(flattenedSchema , fieldName)));
                        } else {
                            joinResults(bTreeFilterService.performNonUniqueBTreeFilter(indexFileContent, condition , getFieldType(flattenedSchema , fieldName)));
                        }
                    } else {
                        System.out.println("Field NOT indexed: " + fieldName + " -> filtering full docs");
                        ObjectNode fullCondition = mapper.createObjectNode();
                        fullCondition.set(fieldName, condition);
                        joinResults(filterNonIndexedFields(fullCondition, collectionPath));
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

    private String getFieldType(JsonNode flattenedSchema , String fieldName) throws JsonProcessingException {
        SchemaRule schemaRule = mapper.treeToValue(flattenedSchema.get(fieldName), SchemaRule.class);
        return schemaRule.getType().toString().toLowerCase();
    }

    private void joinResults(List<String> bTreeResults) {
        System.out.println("Joining results: " + bTreeResults);
        resultDocs.addAll(bTreeResults);
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
