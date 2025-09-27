package com.App.json_explorer_api.controller;

import com.App.json_explorer_api.dto.CollectionMeta;
import com.App.json_explorer_api.dto.CollectionRequest;
import com.App.json_explorer_api.dto.DirectoryRenameRequest;
import com.App.json_explorer_api.service.CollectionApiService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CollectionApiController {

    private final CollectionApiService collectionApiService;

    public CollectionApiController(CollectionApiService collectionApiService) {
        this.collectionApiService = collectionApiService;
    }

    @PostMapping("/load-collections")
    public ResponseEntity<?> loadCollections(@RequestBody JsonNode dbName, HttpServletRequest request) throws Exception {
        List<CollectionMeta> collections = (List<CollectionMeta>) collectionApiService.loadCollections(request, dbName);
        return ResponseEntity.ok(collections);
    }

    @PostMapping("/create-collection/{databaseName}")
    public ResponseEntity<String> createCollection(
            @PathVariable String databaseName,
            @RequestBody CollectionRequest collectionRequest,
            HttpServletRequest request
    ) {
        return collectionApiService.createCollection(databaseName, collectionRequest, request);
    }

    @DeleteMapping("/delete-collection/{databaseName}")
    public ResponseEntity<String> deleteCollection(
            @PathVariable String databaseName,
            @RequestBody CollectionRequest collectionRequest,
            HttpServletRequest request
    ) {
        return collectionApiService.deleteCollection(databaseName, collectionRequest, request);
    }

    @PutMapping("/rename-collection/{databaseName}")
    public ResponseEntity<String> renameCollection(
            @PathVariable String databaseName,
            @RequestBody DirectoryRenameRequest renameRequest,
            HttpServletRequest request
    ) {
        return collectionApiService.renameCollection(databaseName, renameRequest, request);
    }
}
