package com.App.json_explorer_api.controller;

import com.App.json_explorer_api.dto.SignUpRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api")
public class SignupApiController {

    private final RestTemplate restTemplate;

    private final String DB_SIGNUP_URL = "http://localhost:8080/api/signup";

    public SignupApiController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody SignUpRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<SignUpRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                DB_SIGNUP_URL,
                HttpMethod.POST,
                entity,
                String.class
        );

        System.out.println("Response from DB backend: " + response.getBody());
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }
}