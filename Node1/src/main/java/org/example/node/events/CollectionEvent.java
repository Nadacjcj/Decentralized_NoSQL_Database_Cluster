package org.example.node.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import org.example.node.dto.CollectionMeta;
import org.example.node.dto.CollectionRequest;
import org.example.node.service.CollectionIndexService;
import org.example.node.service.CollectionManagementService;
import org.example.node.service.JsonIndexingService;

import java.io.IOException;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class CollectionEvent implements Serializable {
    private String userFolderName;
    private String databaseName;


    public abstract void process(
            CollectionManagementService collectionManagementService,
            CollectionIndexService collectionIndexService,
            JsonIndexingService jsonIndexingService
    ) throws IOException;

    public CollectionEvent(){}

    public String getUserFolderName() {
        return userFolderName;
    }

    public void setUserFolderName(String userFolderName) {
        this.userFolderName = userFolderName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    @Override
    public String toString() {
        return "CollectionEvent{" +
                "userFolderName='" + userFolderName + '\'' +
                ", databaseName='" + databaseName + '\'' +
                '}';
    }
}
