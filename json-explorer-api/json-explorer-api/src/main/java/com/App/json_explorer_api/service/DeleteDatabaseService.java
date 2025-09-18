package com.App.json_explorer_api.service;

import com.App.json_explorer_api.dto.DBDeletionRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DeleteDatabaseService {

    private final RestTemplate restTemplate = new RestTemplate();

    public ResponseEntity<String> deleteDB(DBDeletionRequest dto, String authHeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (authHeader != null && !authHeader.isEmpty()) {
            headers.set("Authorization", authHeader);
        }

        HttpEntity<DBDeletionRequest> entity = new HttpEntity<>(dto, headers);
        String url = "http://localhost:8080/api/database/delete";

        return restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
    }
}
