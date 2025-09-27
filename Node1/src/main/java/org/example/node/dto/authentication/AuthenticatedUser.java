package org.example.node.dto.authentication;

import lombok.Data;

@Data
public class AuthenticatedUser {
    private final String id;
    private final String username;

    public AuthenticatedUser(String id, String username) {
        this.id = id;
        this.username = username;
    }
}
