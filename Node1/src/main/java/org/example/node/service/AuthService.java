package org.example.node.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.node.dto.LoginRequest;
import org.example.node.model.User;
import org.example.node.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    private static final String USERS_FILE = "user_data/users_info.json";
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public Map<String, Object> login(LoginRequest authRequest) throws Exception {
        List<User> users = mapper.readValue(new File(USERS_FILE), new TypeReference<List<User>>() {});

        Optional<User> foundUser = users.stream()
                .filter(u -> u.getUsername().equals(authRequest.getUsername()))
                .findFirst();

        if (foundUser.isPresent()) {
            User user = foundUser.get();

            if (!passwordEncoder.matches(authRequest.getPassword(), user.getPasswordHash())) {
                throw new RuntimeException("Invalid username or password");
            }

            String token = jwtUtil.generateToken(user.getId(), user.getUsername());
            return Map.of(
                    "token", token,
                    "id", user.getId(),
                    "username", user.getUsername()
            );
        } else {
            throw new RuntimeException("Invalid username or password");
        }
    }


    public Map<String, Object> validateToken(String token) {
        if (jwtUtil.validateToken(token)) {
            String username = jwtUtil.extractUsername(token);
            String id = jwtUtil.extractId(token);
            return Map.of(
                    "id", id,
                    "username", username,
                    "message", "Hello, " + username + "!"
            );
        } else {
            throw new RuntimeException("Invalid Token");
        }
    }

}
