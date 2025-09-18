package com.App.json_explorer_api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.App.json_explorer_api.dto.CollectionMeta;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class LoadCollectionsService {
    private final RestTemplate restTemplate = new RestTemplate();

    public List<CollectionMeta> loadCollections(HttpServletRequest request, JsonNode dbName) throws Exception {
        JsonNode collectionsMeta = loadCollectionsMeta(request, dbName);
        return tokeniseJsonToList(collectionsMeta);
    }

    private JsonNode loadCollectionsMeta(HttpServletRequest request, JsonNode dbName) {
        String url = "http://localhost:8080/api/collection/load";
        String authHeader = request.getHeader("Authorization");

        HttpHeaders headers = new HttpHeaders();
        if (authHeader != null && !authHeader.isEmpty()) {
            headers.set("Authorization", authHeader);
        }

        HttpEntity<JsonNode> entity = new HttpEntity<>(dbName, headers);
        ResponseEntity<JsonNode> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                JsonNode.class
        );

        return response.getBody();
    }

    private List<CollectionMeta> tokeniseJsonToList(JsonNode collectionsMeta) {
        List<CollectionMeta> collectionList = new ArrayList<>();

        if (collectionsMeta.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = collectionsMeta.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                JsonNode collNode = entry.getValue();

                CollectionMeta dto = new CollectionMeta(
                        collNode.get("collectionName").asText(),
                        collNode.get("collectionId").asText(),
                        collNode.get("schema")
                );
                collectionList.add(dto);
            }
        }
        return collectionList;
    }
}
