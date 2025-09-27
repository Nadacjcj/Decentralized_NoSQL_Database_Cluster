package org.example.node.dto.queries;

import com.fasterxml.jackson.databind.JsonNode;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FindQueryRequest {

    @JsonProperty("filter")
    private JsonNode filter;

    public FindQueryRequest(JsonNode filter) {
        this.filter = filter;
    }

}
