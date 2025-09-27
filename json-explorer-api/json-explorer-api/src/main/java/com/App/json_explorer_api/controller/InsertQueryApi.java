package com.App.json_explorer_api.controller;

import com.App.json_explorer_api.service.InsertQueryService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class InsertQueryApi {

    private final InsertQueryService insertQueryService;

    public InsertQueryApi(InsertQueryService insertQueryService) {
        this.insertQueryService = insertQueryService;
    }

    @PostMapping("/insertQuery")
    public ResponseEntity<String> handleInsertQuery(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody JsonNode request,
            @RequestParam String databaseName,
            @RequestParam String collectionName
    ) {
        String result = insertQueryService.executeInsertQuery(authHeader, request, databaseName, collectionName);
        return ResponseEntity.ok(result);
    }
}
