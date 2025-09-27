package com.App.json_explorer_api.controller;

import com.App.json_explorer_api.service.DeleteQueryService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class DeleteQueryApi {

    private final DeleteQueryService deleteQueryService;

    public DeleteQueryApi(DeleteQueryService deleteQueryService) {
        this.deleteQueryService = deleteQueryService;
    }

    @PostMapping("/deleteQuery")
    public ResponseEntity<String> handleDeleteQuery(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody JsonNode request,
            @RequestParam String databaseName,
            @RequestParam String collectionName
    ) {
        String result = deleteQueryService.executeDeleteQuery(authHeader, request, databaseName, collectionName);
        return ResponseEntity.ok(result);
    }
}
