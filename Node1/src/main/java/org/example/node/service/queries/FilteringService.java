package org.example.node.service.queries;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.example.node.repository.JsonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.HashSet;

@Service
public class FilteringService {

    public static final String SOMETHING_WENT_WRONG_DURING_INDEXED_FILTER_APPLICATION = "Something went wrong during indexed filter application";
    @Autowired
    private UnionService unionService;
    @Autowired
    private IntersectionService intersectionService;

    @Autowired
    DocsFilterService docsFilterService;
    @Autowired
    private JsonRepository jsonRepository;

    public HashSet<String> applyFilter(JsonNode filter , JsonNode flattenedSchema , Path collectionPath){
        HashSet<String> indexingResult = new HashSet<>();
        try {
            indexingResult = intersectIndexedFilterResult(applyIndexedUnionFieldsFilter(filter , flattenedSchema , collectionPath)
                    ,applyIndexedIntersectionFieldsFilter(filter , flattenedSchema , collectionPath));

        }catch (Exception e) {
            System.out.println(SOMETHING_WENT_WRONG_DURING_INDEXED_FILTER_APPLICATION);
        }
        return indexingResult;

    }
    public HashSet<String> applyIndexedUnionFieldsFilter(JsonNode filter , JsonNode flattenedSchema , Path collectionPath) throws JsonProcessingException {
        return unionService.performUnion(filter , flattenedSchema , collectionPath);
    }
    public HashSet<String> applyIndexedIntersectionFieldsFilter(JsonNode filter , JsonNode flattenedSchema , Path collectionPath) throws JsonProcessingException {
        return intersectionService.performIntersection(filter , flattenedSchema , collectionPath);
    }
    private HashSet<String> intersectIndexedFilterResult(HashSet<String> union, HashSet<String> intersect) {

        if (union == null) {
            return intersect == null ? new HashSet<>() : new HashSet<>(intersect);
        }
        if (intersect == null) {
            return new HashSet<>(union);
        }
        HashSet<String> result = new HashSet<>(union);
        result.retainAll(intersect);

        return result;
    }



}
