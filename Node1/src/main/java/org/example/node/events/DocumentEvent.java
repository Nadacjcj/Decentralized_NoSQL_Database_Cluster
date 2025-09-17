package org.example.node.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.example.node.repository.JsonRepository;
import org.example.node.service.DocumentDeletionManager;
import org.example.node.service.DocumentUpdaterService;
import org.example.node.service.IndexUpdaterService;
import org.example.node.service.JsonIndexingService;

import java.io.IOException;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class DocumentEvent implements Serializable {
    private String userFolderName;
    private String databaseName;
    private String collectionName;
    private String documentId;
    public abstract void process(JsonIndexingService jis , JsonRepository jsr , DocumentDeletionManager ddm , DocumentUpdaterService dus , IndexUpdaterService ius) throws IOException;
    public DocumentEvent(){}

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

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}
