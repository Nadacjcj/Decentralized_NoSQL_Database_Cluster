package org.example.node.util.jsonflattener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JsonFlattener {
    private final ObjectMapper mapper;

    public JsonFlattener(ObjectMapper mapper) {
        this.mapper = mapper;
    }
    public ObjectNode flatten(JsonNode node, FlattenStrategy strategy) throws IOException {
        ObjectNode result = mapper.createObjectNode();
        strategy.flatten("", node, result);
        return result;
    }
}
