package com.App.json_explorer_api.controller;

import com.App.json_explorer_api.service.UpdateQueryService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class UpdateQueryApi {

    private final UpdateQueryService updateQueryService;

    public UpdateQueryApi(UpdateQueryService updateQueryService) {
        this.updateQueryService = updateQueryService;
    }

    @PostMapping("/updateQuery")
    public ResponseEntity<String> handleUpdateQuery(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody JsonNode request,
            @RequestParam String databaseName,
            @RequestParam String collectionName
    ) {
        String result = updateQueryService.executeUpdateQuery(authHeader, request, databaseName, collectionName);
        return ResponseEntity.ok(result);
    }
}
