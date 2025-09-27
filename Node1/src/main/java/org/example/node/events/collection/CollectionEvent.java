package org.example.node.events.collection;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.node.service.collection.CollectionIndexService;
import org.example.node.service.collection.CollectionManagementService;
import org.example.node.service.indexing.JsonIndexingService;

import java.io.IOException;
import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public abstract class CollectionEvent implements Serializable {
    private String userFolderName;
    private String databaseName;


    public abstract void process(
            CollectionManagementService collectionManagementService,
            CollectionIndexService collectionIndexService,
            JsonIndexingService jsonIndexingService
    ) throws IOException;
}
