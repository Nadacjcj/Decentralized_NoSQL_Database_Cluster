package org.example.node.controller;

import org.example.node.dto.LoginRequest;
import org.example.node.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest authRequest) {
        try {
            return ResponseEntity.ok(authService.login(authRequest));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(java.util.Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", "Server error: " + e.getMessage()));
        }
    }
    @GetMapping("/hello")
    public ResponseEntity<?> hello(){
        return ResponseEntity.ok("hello works now");
    }

}
