package com.App.json_explorer_api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class InsertQueryService {

    private final ObjectMapper mapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    public String executeInsertQuery(String authHeader, JsonNode request, String databaseName, String collectionName) {
        JsonNode documentNode = request.has("document") ? request.get("document") : request;

        String url = String.format("http://localhost:8080/api/query/%s/%s/insert", databaseName, collectionName);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", authHeader);

        HttpEntity<JsonNode> entity = new HttpEntity<>(mapper.createObjectNode().set("document", documentNode), headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
        );

        return response.getBody();
    }
}
