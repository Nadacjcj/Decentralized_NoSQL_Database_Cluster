package org.example.node.util.jsonflattener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class DocumentFlattenStrategy implements FlattenStrategy {

    @Override
    public void flatten(String prefix, JsonNode document, ObjectNode result) throws IOException {
        Iterator<Map.Entry<String, JsonNode>> fields = document.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String fieldName = entry.getKey();
            JsonNode value = entry.getValue();

            String fullKey = prefix.isEmpty() ? fieldName : prefix + "." + fieldName;

            if (value.isObject()) {
                flatten(fullKey, value, result);
            } else {
                result.set(fullKey, value);
            }
        }
    }
}
