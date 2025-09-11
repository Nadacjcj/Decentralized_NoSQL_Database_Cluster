package org.example.node.controller;

import org.example.node.dto.DBDeletionRequest;
import org.example.node.dto.DirectoryRenameRequest;
import org.example.node.dto.DBCreationRequest;
import org.example.node.filter.JwtAuthenticationFilter;
import org.example.node.service.DatabaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/database")
public class DatabaseController {

    private final DatabaseService databaseService;

    public DatabaseController(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    private JwtAuthenticationFilter.UserPrincipal getUserPrincipal(Authentication authentication) {
        return (JwtAuthenticationFilter.UserPrincipal) authentication.getPrincipal();
    }

    @PostMapping("/create")
    public ResponseEntity<String> createDatabase(
            Authentication authentication,
            @RequestBody DBCreationRequest request) throws IOException {

        JwtAuthenticationFilter.UserPrincipal user = getUserPrincipal(authentication);
        return ResponseEntity.ok(
                databaseService.createDatabase(user, request)
        );
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteDatabase(
            Authentication authentication,
            @RequestBody DBDeletionRequest request) throws IOException {

        JwtAuthenticationFilter.UserPrincipal user = getUserPrincipal(authentication);
        return ResponseEntity.ok(
                databaseService.deleteDatabase(user, request)
        );
    }

    @PutMapping("/rename")
    public ResponseEntity<String> renameDatabase(
            Authentication authentication,
            @RequestBody DirectoryRenameRequest request) throws IOException {

        JwtAuthenticationFilter.UserPrincipal user = getUserPrincipal(authentication);
        return ResponseEntity.ok(
                databaseService.renameDatabase(user, request)
        );
    }
}
