package org.example.node.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

@Component
public class JsonFlattener {
    private final ObjectMapper mapper = new ObjectMapper();

    public ObjectNode flatten(JsonNode node, FlattenStrategy strategy) throws IOException {
        ObjectNode result = mapper.createObjectNode();
        strategy.flatten("", node, result);
        return result;
    }
}
