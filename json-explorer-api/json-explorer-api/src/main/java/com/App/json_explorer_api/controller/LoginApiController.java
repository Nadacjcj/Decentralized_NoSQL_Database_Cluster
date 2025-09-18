package com.App.json_explorer_api.controller;

import com.App.json_explorer_api.dto.LoginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api")
public class LoginApiController {

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/login")
    public ResponseEntity<?> forwardLogin(@RequestBody LoginRequest authRequest) {
        String url = "http://localhost:8080/api/login";
        return restTemplate.postForEntity(url, authRequest, Object.class);
    }
}
