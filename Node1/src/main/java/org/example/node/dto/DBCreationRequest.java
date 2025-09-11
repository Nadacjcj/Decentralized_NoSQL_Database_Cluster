package org.example.node.dto;

import java.io.Serializable;
import java.util.Date;

public class DBCreationRequest implements Serializable {
    private String databaseName;
    private String description;

    public DBCreationRequest(){}
    public DBCreationRequest(String databaseName) {
        this.databaseName = databaseName;
    }
    public DBCreationRequest(String databaseName , String description) {
        this.databaseName = databaseName;
        this.description = description;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
