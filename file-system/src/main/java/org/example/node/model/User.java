package org.example.node.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.UUID;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class User implements Serializable {
    private String id;
    private String username;
    private String passwordHash;

    @JsonProperty("affinityNode")
    private String affinityNode;

    public User() {}

    public User(String username, String passwordHash, String affinityNode) {
        this.id = UUID.randomUUID().toString();
        this.username = username;
        this.passwordHash = passwordHash;
        this.affinityNode = affinityNode;
    }

    public User(String username, String passwordHash) {
        this(username, passwordHash, null);
    }

}
