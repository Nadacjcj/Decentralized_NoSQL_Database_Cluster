package org.example.node.dto;

import java.io.Serializable;

public class DBDeletionRequest implements Serializable {
    String databaseName;

    public DBDeletionRequest() {
    }

    public DBDeletionRequest(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }
}
