package org.example.node.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User implements Serializable {
    private String id;
    private String username;
    private String passwordHash;

    @JsonProperty("affinityNode")
    private String affinityNode;

    public User() {
        // for Jackson
    }

    public User(String username, String passwordHash, String affinityNode) {
        this.id = UUID.randomUUID().toString();
        this.username = username;
        this.passwordHash = passwordHash;
        this.affinityNode = affinityNode;
    }

    // existing constructor (for backward compat)
    public User(String username, String passwordHash) {
        this(username, passwordHash, null);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getAffinityNode() { return affinityNode; }
    public void setAffinityNode(String affinityNode) { this.affinityNode = affinityNode; }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", passwordHash='" + passwordHash + '\'' +
                ", affinityNode='" + affinityNode + '\'' +
                '}';
    }
}
