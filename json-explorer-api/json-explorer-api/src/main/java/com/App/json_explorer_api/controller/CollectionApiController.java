package com.App.json_explorer_api.controller;

import com.App.json_explorer_api.dto.CollectionMeta;
import com.App.json_explorer_api.dto.CollectionRequest;
import com.App.json_explorer_api.dto.DirectoryRenameRequest;
import com.App.json_explorer_api.service.LoadCollectionsService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CollectionApiController {

    private final LoadCollectionsService loadCollectionsService;
    private final RestTemplate restTemplate;

    public CollectionApiController(LoadCollectionsService loadCollectionsService) {
        this.loadCollectionsService = loadCollectionsService;
        this.restTemplate = new RestTemplate();
    }

    @PostMapping("/load-collections")
    public ResponseEntity<List<CollectionMeta>> loadCollections(
            @RequestBody JsonNode dbName,
            HttpServletRequest request
    ) throws Exception {
        List<CollectionMeta> collections = loadCollectionsService.loadCollections(request, dbName);
        return ResponseEntity.ok(collections);
    }

    @DeleteMapping("/delete-collection/{databaseName}")
    public ResponseEntity<String> deleteCollection(
            @PathVariable String databaseName,
            @RequestBody CollectionRequest collectionRequest
    ) {
        String url = "http://localhost:8080/api/collection/" + databaseName + "/delete";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<CollectionRequest> requestEntity = new HttpEntity<>(collectionRequest, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, String.class);

        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    @PutMapping("/rename-collection/{databaseName}")
    public ResponseEntity<String> renameCollection(
            @PathVariable String databaseName,
            @RequestBody DirectoryRenameRequest renameRequest
    ) {
        String url = "http://localhost:8080/api/collection/" + databaseName + "/rename";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<DirectoryRenameRequest> requestEntity = new HttpEntity<>(renameRequest, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, String.class);

        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }
}
