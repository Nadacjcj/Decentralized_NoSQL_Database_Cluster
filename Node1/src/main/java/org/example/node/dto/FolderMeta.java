package org.example.node.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class FolderMeta implements Serializable {
    private String id;
    private String name;
    private String createdAt;
    private String description;
    public FolderMeta() {}

    public FolderMeta(String id, String name, String createdAt, String description) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "FolderMeta{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
