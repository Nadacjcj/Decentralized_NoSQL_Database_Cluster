package org.example.node.dto.queries;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DeleteDocRequest implements Serializable {
    private JsonNode filter;

    DeleteDocRequest( JsonNode filter) {
        this.filter = filter;
    }

}

