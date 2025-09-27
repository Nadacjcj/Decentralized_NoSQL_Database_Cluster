package org.example.node.dto.collection;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CollectionRequest implements Serializable {
    private String collectionName;
    private JsonNode schema;

    public CollectionRequest(String collectionName) {
        this.collectionName = collectionName;
    }

    public CollectionRequest(String collectionName , JsonNode schema) {
        this.collectionName = collectionName;
        this.schema = schema;
    }

}
