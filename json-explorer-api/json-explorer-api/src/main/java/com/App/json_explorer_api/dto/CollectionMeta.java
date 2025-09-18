package com.App.json_explorer_api.dto;


import com.fasterxml.jackson.databind.JsonNode;

import java.io.Serializable;

public class CollectionMeta  {
    String collectionName;
    String collectionId;
    JsonNode schema;

    public CollectionMeta() {}
    public CollectionMeta(String collectionName , String collectionId , JsonNode schema) {
        this.collectionName = collectionName;
        this.collectionId = collectionId;
        this.schema = schema;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public String getCollectionId() {
        return collectionId;
    }

    public JsonNode getSchema() {
        return schema;
    }

    @Override
    public String toString() {
        return "CollectionMeta{" +
                "collectionName='" + collectionName + '\'' +
                ", collectionId='" + collectionId + '\'' +
                ", schema=" + schema +
                '}';
    }

    public void setName(String newCollectionName) {
        this.collectionName = newCollectionName;
    }
}
