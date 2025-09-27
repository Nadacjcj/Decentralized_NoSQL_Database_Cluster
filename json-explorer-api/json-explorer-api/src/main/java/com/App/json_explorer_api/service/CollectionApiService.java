package com.App.json_explorer_api.service;

import com.App.json_explorer_api.dto.CollectionRequest;
import com.App.json_explorer_api.dto.DirectoryRenameRequest;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class CollectionApiService {

    private final RestTemplate restTemplate;
    private final LoadCollectionsService loadCollectionsService;

    public CollectionApiService(LoadCollectionsService loadCollectionsService) {
        this.loadCollectionsService = loadCollectionsService;
        this.restTemplate = new RestTemplate();
    }

    public List<?> loadCollections(HttpServletRequest request, JsonNode dbName) throws Exception {
        return loadCollectionsService.loadCollections(request, dbName);
    }

    public ResponseEntity<String> createCollection(String databaseName, CollectionRequest collectionRequest, HttpServletRequest request) {
        String url = "http://localhost:8080/api/collection/" + databaseName + "/create";
        return exchangeRequest(url, HttpMethod.POST, collectionRequest, request);
    }

    public ResponseEntity<String> deleteCollection(String databaseName, CollectionRequest collectionRequest, HttpServletRequest request) {
        String url = "http://localhost:8080/api/collection/" + databaseName + "/delete";
        return exchangeRequest(url, HttpMethod.DELETE, collectionRequest, request);
    }

    public ResponseEntity<String> renameCollection(String databaseName, DirectoryRenameRequest renameRequest, HttpServletRequest request) {
        String url = "http://localhost:8080/api/collection/" + databaseName + "/rename";
        return exchangeRequest(url, HttpMethod.PUT, renameRequest, request);
    }

    private <T> ResponseEntity<String> exchangeRequest(String url, HttpMethod method, T body, HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && !authHeader.isEmpty()) {
            headers.set("Authorization", authHeader);
        }

        HttpEntity<T> requestEntity = new HttpEntity<>(body, headers);
        return restTemplate.exchange(url, method, requestEntity, String.class);
    }
}
