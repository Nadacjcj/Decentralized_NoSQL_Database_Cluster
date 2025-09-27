package org.example.node.service.collection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.node.dto.collection.SchemaRule;
import org.example.node.enums.FieldTypes;
import org.example.node.repository.JsonRepository;
import org.example.node.service.indexing.JsonIndexingService;
import org.example.node.util.jsonflattener.DocumentFlattenStrategy;
import org.example.node.util.filesystem.JsonFileLister;
import org.example.node.util.jsonflattener.JsonFlattener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Iterator;

@Service
public class SchemaValidationService {
    public static final String DOCUMENTS = "documents";
    private final ObjectMapper mapper;
    @Autowired
    JsonRepository jsonRepository;
    @Autowired
    JsonFlattener jsonFlattener;

    public SchemaValidationService(ObjectMapper mapper) {
        this.mapper = mapper;
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true);
    }

    public boolean validateType(JsonNode document, JsonNode schema) throws IOException {
        Iterator<String> fieldNames = schema.fieldNames();

        while (fieldNames.hasNext()) {
            String key = fieldNames.next();
            JsonNode valueNode = schema.get(key);
            SchemaRule schemaRule = mapper.treeToValue(valueNode, SchemaRule.class);

            JsonNode documentValue = document.get(key);

            if (documentValue != null && !documentValue.isNull()) {
                if ((schemaRule.getType() == FieldTypes.INTEGER) && !documentValue.isInt()) {
                    return false;
                }else if ((schemaRule.getType() == FieldTypes.STRING) && !documentValue.isTextual()) {
                    return false;
                } else if ((schemaRule.getType() == FieldTypes.BOOLEAN) && !documentValue.isBoolean()) {
                    return false;
                }else if ((schemaRule.getType() == FieldTypes.FLOAT) && !(documentValue.isFloat() || documentValue.isDouble())) {
                    return false;
                } else if ((schemaRule.getType() == FieldTypes.OBJECT) && !documentValue.isObject()) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean validateRequired(JsonNode document, JsonNode schema) throws IOException {
        Iterator<String> fieldNames = schema.fieldNames();
        while (fieldNames.hasNext()) {
            String key = fieldNames.next();

            JsonNode valueNode = schema.get(key);
            SchemaRule schemaRule = mapper.treeToValue(valueNode, SchemaRule.class);
            Boolean requiredRule = schemaRule.isRequired();
            JsonNode documentValue = document.get(key);

            boolean isRequired = (requiredRule == null) ? false : schemaRule.isRequired();
            System.out.println(isRequired);
            if (isRequired && (documentValue == null || documentValue.isNull())) {
                return false;
            }

        }
        return true;
    }

    public boolean validateUniques(JsonNode document, JsonNode schema, Path collectionPath) throws IOException {
        HashSet<String> indexFileNames = JsonFileLister.listJsonFileNames(collectionPath);
        Path documentsPath = collectionPath.resolve(DOCUMENTS);
        HashSet<Path> docFilePaths = JsonFileLister.listJsonFilePaths(documentsPath);

        Iterator<String> fieldNames = document.fieldNames();
        while (fieldNames.hasNext()) {
            String key = fieldNames.next();

            JsonNode documentValueNode = (document.get(key));
            JsonNode schemaValueNode = schema.get(key);
            SchemaRule schemaRule = mapper.treeToValue(schemaValueNode, SchemaRule.class);

            if (schemaRule.isUnique() == Boolean.TRUE) {

                String encodedFilename = JsonIndexingService.encodeFileName(key);
                if(indexFileNames.contains(encodedFilename)){
                    Path indexFilePath = collectionPath.resolve(encodedFilename);
                    JsonNode indexFile = jsonRepository.readIndex(indexFilePath);

                    if (indexFile.has(documentValueNode.asText())) {
                        return false;
                    }
                }else {
                    if(docFilePaths.isEmpty()) {
                        return true;
                    }
                    for(Path docFilePath : docFilePaths) {
                        JsonNode documentFile = jsonRepository.readIndex(docFilePath);
                        JsonNode flattenedDocument = jsonFlattener.flatten(documentFile , new DocumentFlattenStrategy());

                        if (flattenedDocument.get(key).asText().equals(document.get(key).asText())) {
                            return false;
                        }
                    }
                    return true;
                    //lookup through all files
                }
            }
        }
        return true;
    }

}
