package com.App.json_explorer_api.dto;


public class DBDeletionRequest  {
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