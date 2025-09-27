package com.App.json_explorer_api.controller;

import com.App.json_explorer_api.service.FindQueryService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class FindQueryApi {

    private final FindQueryService findQueryService;

    public FindQueryApi(FindQueryService findQueryService) {
        this.findQueryService = findQueryService;
    }

    @PostMapping("/findQuery")
    public ResponseEntity<List<JsonNode>> handleFindQuery(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody JsonNode request,
            @RequestParam String databaseName,
            @RequestParam String collectionName
    ) {
        List<JsonNode> result = findQueryService.executeFindQuery(authHeader, request, databaseName, collectionName);
        return ResponseEntity.ok(result);
    }
}
