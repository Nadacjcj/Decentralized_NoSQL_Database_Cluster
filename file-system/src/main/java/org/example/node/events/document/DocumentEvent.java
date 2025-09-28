package org.example.node.events.document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.node.repository.JsonRepository;
import org.example.node.service.queries.DocumentDeletionManager;
import org.example.node.service.queries.DocumentUpdaterService;
import org.example.node.service.indexing.IndexUpdaterService;
import org.example.node.service.indexing.JsonIndexingService;

import java.io.IOException;
import java.io.Serializable;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class DocumentEvent implements Serializable {
    private String userFolderName;
    private String databaseName;
    private String collectionName;
    private String documentId;
    public abstract void process(JsonIndexingService jis , JsonRepository jsr , DocumentDeletionManager ddm , DocumentUpdaterService dus , IndexUpdaterService ius) throws IOException;

}
