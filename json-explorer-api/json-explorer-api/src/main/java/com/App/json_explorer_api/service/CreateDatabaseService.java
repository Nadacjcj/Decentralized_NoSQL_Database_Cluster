package com.App.json_explorer_api.service;

import com.App.json_explorer_api.dto.DBCreationRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CreateDatabaseService {

    private final RestTemplate restTemplate = new RestTemplate();

    public String createDB(DBCreationRequest dto, String authToken) {
        String url = "http://localhost:8080/api/database/create";
        DBCreationRequest requestBody = new DBCreationRequest(dto.getDatabaseName(), dto.getDescription());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (authToken != null && !authToken.isEmpty()) {
            headers.set("Authorization", authToken);
        }

        HttpEntity<DBCreationRequest> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        return response.getBody();
    }
}
