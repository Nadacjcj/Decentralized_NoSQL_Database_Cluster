package com.App.json_explorer_api.controller;

import com.App.json_explorer_api.dto.DBCreationRequest;
import com.App.json_explorer_api.dto.DBDeletionRequest;
import com.App.json_explorer_api.dto.DatabaseMeta;
import com.App.json_explorer_api.dto.DirectoryRenameRequest;
import com.App.json_explorer_api.service.CreateDatabaseService;
import com.App.json_explorer_api.service.LoadDatabasesService;
import com.App.json_explorer_api.service.DeleteDatabaseService;
import com.App.json_explorer_api.service.RenameDatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api")
public class DatabaseApiController {

    @Autowired private LoadDatabasesService loadDatabasesService;
    @Autowired private CreateDatabaseService createDatabaseService;
    @Autowired private DeleteDatabaseService deleteDatabaseService;
    @Autowired private RenameDatabaseService renameDatabaseService;

    @GetMapping("/load-databases")
    public ResponseEntity<List<DatabaseMeta>> loadDatabases(HttpServletRequest request) throws Exception {
        return ResponseEntity.ok(loadDatabasesService.loadDatabases(request));
    }

    @PostMapping("/create-database")
    public ResponseEntity<?> createDatabase(@RequestBody DBCreationRequest dto,
                                            HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            return ResponseEntity.ok(createDatabaseService.createDB(dto, authHeader));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to create database: " + e.getMessage());
        }
    }

    @PostMapping("/delete-database")
    public ResponseEntity<?> deleteDatabase(@RequestBody DBDeletionRequest dto,
                                            HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            return deleteDatabaseService.deleteDB(dto, authHeader);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to delete database: " + e.getMessage());
        }
    }

    @PostMapping("/rename-database")
    public ResponseEntity<?> renameDatabase(@RequestBody DirectoryRenameRequest dto,
                                            HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            return renameDatabaseService.renameDB(dto, authHeader);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to rename database: " + e.getMessage());
        }
    }
}
