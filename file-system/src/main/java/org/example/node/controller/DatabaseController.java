package org.example.node.controller;

import org.example.node.dto.database.DBDeletionRequest;
import org.example.node.dto.database.DirectoryRenameRequest;
import org.example.node.dto.database.DBCreationRequest;
import org.example.node.filter.JwtAuthenticationFilter;
import org.example.node.service.database.DatabaseService;
import org.example.node.service.database.LoadDatabasesService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/database")
public class DatabaseController {

    private final DatabaseService databaseService;
    private final LoadDatabasesService loadDatabasesService;

    public DatabaseController(DatabaseService databaseService, LoadDatabasesService loadDatabasesService) {
        this.databaseService = databaseService;
        this.loadDatabasesService = loadDatabasesService;
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

    @GetMapping("/load")
    public ResponseEntity<?> loadDatabases(
            Authentication authentication) throws IOException {
        JwtAuthenticationFilter.UserPrincipal user = getUserPrincipal(authentication);
        return ResponseEntity.ok(
                loadDatabasesService.loadDBs(user)
        );
    }
}
