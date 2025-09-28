package org.example.node.dto.queries;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateDocRequest implements Serializable {

    private JsonNode filter;
    private JsonNode update;

    public UpdateDocRequest(JsonNode filter, JsonNode update) {
        this.filter = filter;
        this.update = update;
    }

}

