package org.example.node.util.jsonflattener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;

public interface FlattenStrategy {
    void flatten(String prefix, JsonNode node, ObjectNode result) throws IOException;
}
