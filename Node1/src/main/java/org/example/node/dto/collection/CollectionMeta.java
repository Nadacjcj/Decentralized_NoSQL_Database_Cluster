package org.example.node.dto.collection;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CollectionMeta  implements Serializable {
    String collectionName;
    String collectionId;
    JsonNode schema;

    public CollectionMeta(String collectionName , String collectionId , JsonNode schema) {
        this.collectionName = collectionName;
        this.collectionId = collectionId;
        this.schema = schema;
    }

}
