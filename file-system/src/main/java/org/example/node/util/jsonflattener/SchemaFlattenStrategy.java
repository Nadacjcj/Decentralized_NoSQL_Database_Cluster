package org.example.node.util.jsonflattener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class SchemaFlattenStrategy implements FlattenStrategy {

    public static final String SCHEMA_VALIDATION_FAILED_AT_FIELD = "Schema validation failed at field: ";

    @Override
    public void flatten(String prefix, JsonNode schema, ObjectNode result) throws IOException {
        Iterator<Map.Entry<String, JsonNode>> fields = schema.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String fieldName = entry.getKey();
            JsonNode value = entry.getValue();

            if (!followsRule(value)) {
                throw new IllegalArgumentException(SCHEMA_VALIDATION_FAILED_AT_FIELD
                        + (prefix.isEmpty() ? fieldName : prefix + "." + fieldName));
            }

            String fullKey = prefix.isEmpty() ? fieldName : prefix + "." + fieldName;

            if (!"object".equalsIgnoreCase(value.get("type").asText())) {
                result.set(fullKey, value);
            } else {
                ObjectNode nested = value.deepCopy();
                nested.remove("required");
                nested.remove("unique");
                nested.remove("type");
                flatten(fullKey, nested, result);
            }
        }
    }

    private boolean followsRule(JsonNode node) {
        return node.has("type");
    }
}
