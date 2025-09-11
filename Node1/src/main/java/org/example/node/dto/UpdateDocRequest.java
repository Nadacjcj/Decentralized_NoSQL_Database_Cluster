package org.example.node.dto;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.node.repository.JsonRepository;

import java.io.Serializable;
import java.util.Map;

public class UpdateDocRequest implements Serializable {

    private JsonNode filter;
    private JsonNode update;

    public UpdateDocRequest() {
    }

    public UpdateDocRequest(JsonNode filter, JsonNode update) {
        this.filter = filter;
        this.update = update;
    }

    public JsonNode getFilter() {
        return filter;
    }

    public void setFilter(JsonNode filter) {
        this.filter = filter;
    }

    public JsonNode getUpdate() {
        return update;
    }

    public void setUpdate(JsonNode update) {
        this.update = update;
    }
}

