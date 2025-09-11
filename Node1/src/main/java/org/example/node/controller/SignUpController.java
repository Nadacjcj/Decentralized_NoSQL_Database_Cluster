package org.example.node.controller;

import jakarta.validation.Valid;
import org.example.node.dto.SignUpRequest;
import org.example.node.dto.SignUpResponse;
import org.example.node.model.User;
import org.example.node.service.SignUpService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class SignUpController {
    private final SignUpService signUpService;

    public SignUpController(SignUpService signUpService) {
        this.signUpService = signUpService;
    }

    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        try {
            SignUpResponse response = signUpService.signUp(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new SignUpResponse(null, e.getMessage()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("/signup-forward")
    public ResponseEntity<String> signupForward() throws IOException {
        return ResponseEntity.ok(" User Arrived to forward Nodeeeeeeeeee");
    }

}
