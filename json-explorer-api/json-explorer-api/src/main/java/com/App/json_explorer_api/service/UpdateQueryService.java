package com.App.json_explorer_api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class UpdateQueryService {

    private final ObjectMapper mapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    public String executeUpdateQuery(String authHeader, JsonNode request, String databaseName, String collectionName) {
        ObjectNode filterNode = mapper.createObjectNode();
        ObjectNode updateSetNode = mapper.createObjectNode();

        if (request.has("ands") && request.get("ands").size() > 0) {
            for (JsonNode and : request.get("ands")) {
                String field = and.get("field").asText();
                String operator = and.get("operator").asText();
                String valueStr = and.get("value").asText();

                if (!field.isEmpty()) {
                    ObjectNode opNode = mapper.createObjectNode();
                    if (valueStr.matches("-?\\d+")) {
                        opNode.put(operator, Integer.parseInt(valueStr));
                    } else if (valueStr.matches("-?\\d*\\.\\d+")) {
                        opNode.put(operator, Double.parseDouble(valueStr));
                    } else {
                        opNode.put(operator, valueStr);
                    }
                    filterNode.set(field, opNode);
                }
            }
        }

        if (request.has("ors") && request.get("ors").size() > 0) {
            ArrayNode orArray = mapper.createArrayNode();
            for (JsonNode or : request.get("ors")) {
                String field = or.get("field").asText();
                String operator = or.get("operator").asText();
                String valueStr = or.get("value").asText();

                if (!field.isEmpty()) {
                    ObjectNode orItem = mapper.createObjectNode();
                    ObjectNode opNode = mapper.createObjectNode();
                    if (valueStr.matches("-?\\d+")) {
                        opNode.put(operator, Integer.parseInt(valueStr));
                    } else if (valueStr.matches("-?\\d*\\.\\d+")) {
                        opNode.put(operator, Double.parseDouble(valueStr));
                    } else {
                        opNode.put(operator, valueStr);
                    }
                    orItem.set(field, opNode);
                    orArray.add(orItem);
                }
            }
            if (orArray.size() > 0) {
                filterNode.set("or", orArray);
            }
        }

        if (filterNode.isEmpty()) filterNode = mapper.createObjectNode();

        if (request.has("updateFields") && request.get("updateFields").size() > 0) {
            for (JsonNode updateField : request.get("updateFields")) {
                String field = updateField.get("field").asText();
                String valueStr = updateField.get("value").asText();

                if (!field.isEmpty()) {
                    if (valueStr.matches("-?\\d+")) {
                        updateSetNode.put(field, Integer.parseInt(valueStr));
                    } else if (valueStr.matches("-?\\d*\\.\\d+")) {
                        updateSetNode.put(field, Double.parseDouble(valueStr));
                    } else {
                        updateSetNode.put(field, valueStr);
                    }
                }
            }
        }

        ObjectNode body = mapper.createObjectNode();
        body.set("filter", filterNode);
        ObjectNode updateNode = mapper.createObjectNode();
        updateNode.set("set", updateSetNode);
        body.set("update", updateNode);

        System.out.println("Final JSON body to send: " + body.toPrettyString());
        System.out.println("Forwarding header: " + authHeader);

        String url = String.format("http://localhost:8080/api/query/%s/%s/update", databaseName, collectionName);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", authHeader);

        HttpEntity<JsonNode> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);

        return response.getBody();
    }
}
