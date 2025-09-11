package org.example.node.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.Serial;
import java.io.Serializable;

public class CollectionRequest implements Serializable {
    private String collectionName;
    private JsonNode schema;

    public CollectionRequest() {}
    public CollectionRequest(String collectionName) {
        this.collectionName = collectionName;
    }

    public CollectionRequest(String collectionName , JsonNode schema) {
        this.collectionName = collectionName;
        this.schema = schema;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public JsonNode getSchema() {
        return schema;
    }

    public void setSchema(JsonNode schema) {
        this.schema = schema;
    }


}
