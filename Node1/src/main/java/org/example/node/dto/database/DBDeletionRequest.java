package org.example.node.dto.database;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DBDeletionRequest implements Serializable {
    String databaseName;
    public DBDeletionRequest(String databaseName) {
        this.databaseName = databaseName;
    }
}
