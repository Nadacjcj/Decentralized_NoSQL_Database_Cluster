package com.App.json_explorer_api.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api")
public class DeleteQueryApi {

    private final ObjectMapper mapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/deleteQuery")
    public ResponseEntity<String> handleDeleteQuery(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody JsonNode request,
            @RequestParam String databaseName,
            @RequestParam String collectionName
    ) {
        ObjectNode filterNode = mapper.createObjectNode();

        if (request.has("ands") && request.get("ands").size() > 0) {
            for (JsonNode and : request.get("ands")) {
                String field = and.get("field").asText();
                String operator = and.get("operator").asText();
                String valueStr = and.get("value").asText();

                if (!field.isEmpty()) {
                    ObjectNode opNode = mapper.createObjectNode();
                    // Try integer, then double, otherwise string
                    try {
                        int intValue = Integer.parseInt(valueStr);
                        opNode.put(operator, intValue);
                    } catch (NumberFormatException e1) {
                        try {
                            double doubleValue = Double.parseDouble(valueStr);
                            opNode.put(operator, doubleValue);
                        } catch (NumberFormatException e2) {
                            opNode.put(operator, valueStr);
                        }
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
                    try {
                        int intValue = Integer.parseInt(valueStr);
                        opNode.put(operator, intValue);
                    } catch (NumberFormatException e1) {
                        try {
                            double doubleValue = Double.parseDouble(valueStr);
                            opNode.put(operator, doubleValue);
                        } catch (NumberFormatException e2) {
                            opNode.put(operator, valueStr);
                        }
                    }
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

        String url = String.format("http://localhost:8080/api/query/%s/%s/delete", databaseName, collectionName);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", authHeader);

        HttpEntity<JsonNode> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
        );

        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }
}
