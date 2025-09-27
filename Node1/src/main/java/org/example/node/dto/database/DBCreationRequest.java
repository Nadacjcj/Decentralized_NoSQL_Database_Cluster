package org.example.node.dto.database;

import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DBCreationRequest implements Serializable {
    private String databaseName;
    private String description;

    public DBCreationRequest(String databaseName) {
        this.databaseName = databaseName;
    }
    public DBCreationRequest(String databaseName , String description) {
        this.databaseName = databaseName;
        this.description = description;
    }
}
