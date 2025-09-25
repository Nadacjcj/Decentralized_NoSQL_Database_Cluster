package com.App.json_explorer_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api")
public class SchemaQueryApi {

    private final ObjectMapper mapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/get-schema")
    public ResponseEntity<String> getSchema(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestParam String databaseName,
            @RequestParam String collectionName
    ) {
        String url = String.format(
                "http://localhost:8080/api/collection/get-schema/%s/%s?databaseName=%s&collectionName=%s",
                databaseName, collectionName, databaseName, collectionName
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", authHeader);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
        );

        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }
}
