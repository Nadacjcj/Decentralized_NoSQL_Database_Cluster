package org.example.node.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.node.dto.DeleteDocRequest;
import org.example.node.dto.FindQueryRequest;
import org.example.node.dto.UpdateDocRequest;
import org.example.node.filter.JwtAuthenticationFilter;
import org.example.node.service.DeleteDocumentService;
import org.example.node.service.FindQueryService;
import org.example.node.service.InsertDocumentService;
import org.example.node.service.UpdateDocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("api/query")
public class QueryController {

    private final InsertDocumentService insertDocumentService;
    private final DeleteDocumentService deleteDocumentService;
    private final UpdateDocumentService updateDocumentService;
    private final FindQueryService findQueryService;

    public QueryController(InsertDocumentService insertDocumentService,
                           DeleteDocumentService deleteDocumentService,
                           UpdateDocumentService updateDocumentService,
                           FindQueryService findQueryService) {
        this.insertDocumentService = insertDocumentService;
        this.deleteDocumentService = deleteDocumentService;
        this.updateDocumentService = updateDocumentService;
        this.findQueryService = findQueryService;
    }

    private JwtAuthenticationFilter.UserPrincipal getUserPrincipal(Authentication authentication) {
        return (JwtAuthenticationFilter.UserPrincipal) authentication.getPrincipal();
    }

    @PostMapping("/{databaseName}/{collectionName}/insert")
    public ResponseEntity<String> insertDocument(
            Authentication authentication,
            @PathVariable String databaseName,
            @PathVariable String collectionName,
            @RequestBody JsonNode query) throws IOException {

        JwtAuthenticationFilter.UserPrincipal user = getUserPrincipal(authentication);
        return ResponseEntity.ok(
                insertDocumentService.insertDocument(user, databaseName, collectionName, query.get("document"))
        );
    }

    @PostMapping("/{databaseName}/{collectionName}/delete")
    public ResponseEntity<String> deleteDocument(
            Authentication authentication,
            @PathVariable String databaseName,
            @PathVariable String collectionName,
            @RequestBody DeleteDocRequest deleteDocRequest) throws IOException {

        JwtAuthenticationFilter.UserPrincipal user = getUserPrincipal(authentication);
        return ResponseEntity.ok(
                deleteDocumentService.deleteDocument(user, databaseName, collectionName, deleteDocRequest)
        );
    }

    @PutMapping("/{databaseName}/{collectionName}/update")
    public ResponseEntity<String> updateDocument(
            Authentication authentication,
            @PathVariable String databaseName,
            @PathVariable String collectionName,
            @RequestBody UpdateDocRequest updateDocRequest) throws IOException {

        JwtAuthenticationFilter.UserPrincipal user = getUserPrincipal(authentication);
        return ResponseEntity.ok(
                updateDocumentService.updateDocument(user, databaseName, collectionName, updateDocRequest)
        );
    }

    @PostMapping("/{databaseName}/{collectionName}/find")
    public ResponseEntity<List<JsonNode>> readDocuments(
            Authentication authentication,
            @PathVariable String databaseName,
            @PathVariable String collectionName,
            @RequestBody FindQueryRequest findQueryRequest) throws IOException {

        JwtAuthenticationFilter.UserPrincipal user = getUserPrincipal(authentication);
        return ResponseEntity.ok(
                findQueryService.find(user, databaseName, collectionName, findQueryRequest)
        );
    }
}
