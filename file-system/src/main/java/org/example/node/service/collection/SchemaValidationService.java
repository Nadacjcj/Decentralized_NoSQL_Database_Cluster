package org.example.node.service.collection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.node.dto.collection.SchemaRule;
import org.example.node.enums.FieldTypes;
import org.example.node.repository.JsonRepository;
import org.example.node.service.indexing.JsonIndexingService;
import org.example.node.util.filesystem.JsonFileLister;
import org.example.node.util.jsonflattener.DocumentFlattenStrategy;
import org.example.node.util.jsonflattener.JsonFlattener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Service
public class SchemaValidationService {

    private static final Logger logger = LogManager.getLogger(SchemaValidationService.class);

    public static final String DOCUMENTS = "documents";
    private final ObjectMapper mapper;

    @Autowired JsonRepository jsonRepository;
    @Autowired JsonFlattener jsonFlattener;

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
                    logger.warn("Validation failed for field '{}' (expected INTEGER)", key);
                    return false;
                } else if ((schemaRule.getType() == FieldTypes.STRING) && !documentValue.isTextual()) {
                    logger.warn("Validation failed for field '{}' (expected STRING)", key);
                    return false;
                } else if ((schemaRule.getType() == FieldTypes.BOOLEAN) && !documentValue.isBoolean()) {
                    logger.warn("Validation failed for field '{}' (expected BOOLEAN)", key);
                    return false;
                } else if ((schemaRule.getType() == FieldTypes.FLOAT) && !(documentValue.isFloat() || documentValue.isDouble())) {
                    logger.warn("Validation failed for field '{}' (expected FLOAT/DOUBLE)", key);
                    return false;
                } else if ((schemaRule.getType() == FieldTypes.OBJECT) && !documentValue.isObject()) {
                    logger.warn("Validation failed for field '{}' (expected OBJECT)", key);
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
            if (isRequired && (documentValue == null || documentValue.isNull())) {
                logger.warn("Required field '{}' is missing in document", key);
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
            JsonNode documentValueNode = document.get(key);
            JsonNode schemaValueNode = schema.get(key);
            SchemaRule schemaRule = mapper.treeToValue(schemaValueNode, SchemaRule.class);

            if (Boolean.TRUE.equals(schemaRule.isUnique())) {
                String encodedFilename = JsonIndexingService.encodeFileName(key);
                if (indexFileNames.contains(encodedFilename)) {
                    Path indexFilePath = collectionPath.resolve(encodedFilename);
                    JsonNode indexFile = jsonRepository.readIndex(indexFilePath);

                    if (indexFile.has(documentValueNode.asText())) {
                        logger.warn("Unique constraint violated for field '{}' with value '{}'", key, documentValueNode.asText());
                        return false;
                    }
                } else {
                    if (docFilePaths.isEmpty()) {
                        return true;
                    }
                    for (Path docFilePath : docFilePaths) {
                        JsonNode documentFile = jsonRepository.readIndex(docFilePath);
                        JsonNode flattenedDocument = jsonFlattener.flatten(documentFile, new DocumentFlattenStrategy());

                        if (flattenedDocument.get(key).asText().equals(documentValueNode.asText())) {
                            logger.warn("Unique constraint violated for field '{}' with value '{}'", key, documentValueNode.asText());
                            return false;
                        }
                    }
                    return true;
                }
            }
        }
        return true;
    }
}
