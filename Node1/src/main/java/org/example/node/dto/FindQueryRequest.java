package org.example.node.dto;

import com.fasterxml.jackson.databind.JsonNode;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public class FindQueryRequest {

    @JsonProperty("filter")
    private JsonNode filter;

    public FindQueryRequest() {}

    public FindQueryRequest(JsonNode filter) {
        this.filter = filter;
    }

    public JsonNode getFilter() {
        return filter;
    }

    public void setFilter(JsonNode filter) {
        this.filter = filter;
    }
}
