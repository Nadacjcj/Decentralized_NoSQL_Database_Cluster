package org.example.node.dto.database;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FolderMeta implements Serializable {
    private String id;
    private String name;
    private String createdAt;
    private String description;

    public FolderMeta(String id, String name, String createdAt, String description) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.description = description;
    }
}
