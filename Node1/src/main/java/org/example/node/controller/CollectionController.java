package org.example.node.controller;

import org.example.node.dto.CollectionRequest;
import org.example.node.dto.DirectoryRenameRequest;
import org.example.node.filter.JwtAuthenticationFilter;
import org.example.node.service.CollectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/collection")
public class CollectionController {

    private final CollectionService collectionService;

    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    private JwtAuthenticationFilter.UserPrincipal getUserPrincipal(Authentication authentication) {
        return (JwtAuthenticationFilter.UserPrincipal) authentication.getPrincipal();
    }

    @PostMapping("/{databaseName}/create")
    public ResponseEntity<String> createCollection(
            Authentication authentication,
            @PathVariable String databaseName,
            @RequestBody CollectionRequest collectionRequest) throws IOException {

        JwtAuthenticationFilter.UserPrincipal user = getUserPrincipal(authentication);
        return ResponseEntity.ok(collectionService.createCollection(user, databaseName, collectionRequest));
    }

    @DeleteMapping("/{databaseName}/delete")
    public ResponseEntity<String> deleteCollection(
            Authentication authentication,
            @PathVariable String databaseName,
            @RequestBody CollectionRequest collectionRequest) throws IOException {

        JwtAuthenticationFilter.UserPrincipal user = getUserPrincipal(authentication);
        return ResponseEntity.ok(collectionService.deleteCollection(user, databaseName, collectionRequest));
    }

    @PutMapping("/{databaseName}/rename")
    public ResponseEntity<String> renameCollection(
            Authentication authentication,
            @PathVariable String databaseName,
            @RequestBody DirectoryRenameRequest collectionRenameRequest) throws IOException {

        JwtAuthenticationFilter.UserPrincipal user = getUserPrincipal(authentication);
        return ResponseEntity.ok(collectionService.renameCollection(user, databaseName, collectionRenameRequest));
    }
}
