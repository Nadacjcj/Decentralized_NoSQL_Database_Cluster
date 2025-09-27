package org.example.node.service.collection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.node.dto.collection.CollectionMeta;
import org.example.node.dto.database.DirectoryRenameRequest;
import org.example.node.repository.JsonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

@Service
public class CollectionIndexService {

    @Autowired
    private final JsonRepository jsonRepository;
    private final ObjectMapper objectMapper;

    public CollectionIndexService(JsonRepository jsonRepository , ObjectMapper objectMapper) {
        this.jsonRepository = jsonRepository;
        this.objectMapper = objectMapper;

    }

    public void storeCollectionInfo(CollectionMeta collectionMeta, Path jsonFilePath) throws IOException {
        ObjectNode collectionMap = jsonRepository.readIndex(jsonFilePath);
        JsonNode collectionMetaNode = objectMapper.valueToTree(collectionMeta);
        collectionMap.set(collectionMeta.getCollectionName(), collectionMetaNode);
        jsonRepository.writeIndex(collectionMap, jsonFilePath);
    }

    public void removeCollectionInfo(String collectionName, Path jsonFilePath) throws IOException {
        ObjectNode collectionMap = jsonRepository.readIndex(jsonFilePath);
        collectionMap.remove(collectionName);
        jsonRepository.writeIndex(collectionMap, jsonFilePath);
    }

    public void renameCollection(DirectoryRenameRequest collectionRenameRequest, Path jsonFilePath) throws IOException {
        ObjectNode collectionMap = jsonRepository.readIndex(jsonFilePath);

        String oldName = collectionRenameRequest.getOldDirectoryName();
        String newName = collectionRenameRequest.getNewDirectoryName();

        if (collectionMap.has(oldName)) {
            JsonNode collectionMetaNode = collectionMap.get(oldName);
            CollectionMeta collectionMeta = objectMapper.treeToValue(collectionMetaNode, CollectionMeta.class);

            collectionMeta.setCollectionName(newName);

            collectionMap.remove(oldName);
            JsonNode updatedCollectionMetaNode = objectMapper.valueToTree(collectionMeta);
            collectionMap.set(newName, updatedCollectionMetaNode);

            jsonRepository.writeIndex(collectionMap, jsonFilePath);
        } else {
            System.out.println("Key '" + oldName + "' doesn't exist");
        }
    }
}
