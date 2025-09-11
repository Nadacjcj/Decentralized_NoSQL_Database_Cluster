package org.example.node.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public class DeleteDocRequest implements Serializable {
    private JsonNode filter;
    DeleteDocRequest( JsonNode filter) {
        this.filter = filter;
    }
    public DeleteDocRequest() {}

    public JsonNode getFilter() {
        return filter;
    }
    public void setFilter(JsonNode filter) {
        this.filter = filter;
    }
}

