package com.App.json_explorer_api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class FindQueryService {

    private final ObjectMapper mapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    public List<JsonNode> executeFindQuery(String authHeader, JsonNode request, String databaseName, String collectionName) {
        ObjectNode filterNode = mapper.createObjectNode();

        if (request.has("ands") && request.get("ands").size() > 0) {
            for (JsonNode and : request.get("ands")) {
                String field = and.get("field").asText();
                String operator = and.get("operator").asText();
                String value = and.get("value").asText();
                if (!field.isEmpty()) {
                    ObjectNode opNode = mapper.createObjectNode();
                    opNode.put(operator, value);
                    filterNode.set(field, opNode);
                }
            }
        }

        if (request.has("ors") && request.get("ors").size() > 0) {
            ArrayNode orArray = mapper.createArrayNode();
            for (JsonNode or : request.get("ors")) {
                String field = or.get("field").asText();
                String operator = or.get("operator").asText();
                String value = or.get("value").asText();
                if (!field.isEmpty()) {
                    ObjectNode orItem = mapper.createObjectNode();
                    ObjectNode opNode = mapper.createObjectNode();
                    opNode.put(operator, value);
                    orItem.set(field, opNode);
                    orArray.add(orItem);
                }
            }
            if (orArray.size() > 0) {
                filterNode.set("or", orArray);
            }
        }

        if (filterNode.isEmpty()) {
            filterNode = mapper.createObjectNode();
        }

        ObjectNode body = mapper.createObjectNode();
        body.set("filter", filterNode);

        System.out.println("Final JSON body to send: " + body.toPrettyString());
        System.out.println("Forwarding header: " + authHeader);

        String url = String.format("http://localhost:8080/api/query/%s/%s/find", databaseName, collectionName);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", authHeader);

        HttpEntity<JsonNode> entity = new HttpEntity<>(body, headers);

        ResponseEntity<List<JsonNode>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<List<JsonNode>>() {}
        );

        return response.getBody();
    }
}
