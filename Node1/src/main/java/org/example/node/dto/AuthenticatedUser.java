package org.example.node.dto;

public class AuthenticatedUser {
    private final String id;
    private final String username;

    public AuthenticatedUser(String id, String username) {
        this.id = id;
        this.username = username;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }
}
