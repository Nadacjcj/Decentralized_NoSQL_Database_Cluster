package org.example.node.service.queries;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.node.dto.collection.SchemaRule;
import org.example.node.repository.JsonRepository;
import org.example.node.util.filesystem.JsonPayloadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
public class DocumentDeletionManager {

    private static final Logger logger = LogManager.getLogger(DocumentDeletionManager.class);

    public static final String DOCUMENTS = "documents";
    public static final String DOT_ = "_DOT_";
    public static final String INDEX_JSON = "_index.json";
    public static final String JSON = ".json";

    @Autowired
    private JsonRepository jsonRepository;

    private final ObjectMapper mapper;

    public DocumentDeletionManager(ObjectMapper mapper) {
        this.mapper = mapper;
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true);
    }

    public void deleteDocuments(HashSet<String> filteredDocsId, JsonNode flattenedSchema, Path collectionPath) throws IOException {
        logger.info("Deleting documents: {}", filteredDocsId);
        removeFromIndexes(filteredDocsId, flattenedSchema, collectionPath);
        removeActualDocuments(addFileExtension(filteredDocsId), collectionPath);
        logger.info("Deletion completed for documents: {}", filteredDocsId);
    }

    private void removeFromIndexes(HashSet<String> filteredDocsId, JsonNode flattenedSchema, Path collectionPath) throws IOException {
        Iterator<String> fieldNames = flattenedSchema.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();

            if (isIndexed(fieldName, flattenedSchema)) {
                logger.debug("Removing documents from index: {} in field {}", filteredDocsId, fieldName);
                removeDocsFromIndexingFiles(fieldName, filteredDocsId, collectionPath);
            }
        }
    }

    private void removeDocsFromIndexingFiles(String fieldName, HashSet<String> filteredDocsId, Path collectionPath) throws IOException {
        String indexFileName = encodeFileName(fieldName);
        JsonNode indexFileContent = jsonRepository.readIndex(collectionPath.resolve(indexFileName));

        Map<String, JsonNode> indexMap = mapper.convertValue(indexFileContent, new TypeReference<Map<String, JsonNode>>() {});

        Iterator<Map.Entry<String, JsonNode>> iterator = indexMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, JsonNode> entry = iterator.next();
            JsonNode valueNode = entry.getValue();

            if (valueNode.isArray()) {
                List<String> docIds = mapper.convertValue(valueNode, new TypeReference<List<String>>() {});
                docIds.removeIf(filteredDocsId::contains);

                if (docIds.isEmpty()) iterator.remove();
                else entry.setValue(mapper.valueToTree(docIds));

            } else if (valueNode.isTextual()) {
                String docId = valueNode.asText();
                if (filteredDocsId.contains(docId)) iterator.remove();
            }
        }

        jsonRepository.writeIndex(mapper.valueToTree(indexMap), collectionPath.resolve(indexFileName));
        logger.debug("Updated index file: {}", indexFileName);
    }

    private void removeActualDocuments(HashSet<String> filteredDocsId , Path collectionPath) throws IOException {
        Path documentsPath = collectionPath.resolve(DOCUMENTS);
        for(String docId : filteredDocsId) {
            JsonPayloadUtil.deleteJsonDoc(documentsPath.resolve(docId));
            logger.debug("Deleted actual document file: {}", docId);
        }
    }

    private String encodeFileName(String fieldName) throws IOException {
        return fieldName.replace(".", DOT_) + INDEX_JSON;
    }

    private boolean isIndexed(String field , JsonNode flattenedSchema) throws IOException {
        try {
            SchemaRule schemaRule = mapper.treeToValue(flattenedSchema.get(field), SchemaRule.class);
            Boolean indexed = schemaRule.getIndex();
            return indexed != null && indexed;
        } catch (Exception e) {
            logger.error("Error checking if field is indexed: {}", field, e);
            return false;
        }
    }

    private HashSet<String> addFileExtension(HashSet<String> filteredDocsId){
        HashSet<String> plainDocsFilesNames = new HashSet<>();
        for(String docId : filteredDocsId) {
            plainDocsFilesNames.add(docId + JSON);
        }
        return plainDocsFilesNames;
    }
}
