package com.App.json_explorer_api.service;

import com.App.json_explorer_api.dto.DirectoryRenameRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RenameDatabaseService {

    private final RestTemplate restTemplate = new RestTemplate();

    public ResponseEntity<String> renameDB(DirectoryRenameRequest dto, String authHeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (authHeader != null && !authHeader.isEmpty()) {
            headers.set("Authorization", authHeader);
        }

        HttpEntity<DirectoryRenameRequest> entity = new HttpEntity<>(dto, headers);
        String url = "http://localhost:8080/api/database/rename";

        return restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
    }
}
