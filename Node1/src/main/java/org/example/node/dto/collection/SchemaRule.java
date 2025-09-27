package org.example.node.dto.collection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.node.enums.FieldTypes;

public class SchemaRule {
    Boolean isRequired;
    Boolean isUnique;
    FieldTypes type;
    Boolean index;
    @JsonCreator
    public SchemaRule(
            @JsonProperty("required") Boolean isRequired,
            @JsonProperty("unique") Boolean isUnique,
            @JsonProperty("type") FieldTypes type,
            @JsonProperty("index") Boolean index
    ) {
        this.isRequired = isRequired;
        this.isUnique = isUnique;
        this.type = type;
        this.index = index;
    }

    public Boolean isRequired() {
        return isRequired;
    }
    public Boolean isUnique() {
        return isUnique;
    }
    public FieldTypes getType() {
         return type;
    }

    public Boolean getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return "SchemaRule{" +
                "isRequired=" + isRequired +
                ", isUnique=" + isUnique +
                ", type=" + type +
                ", index=" + index +
                '}';
    }
}
