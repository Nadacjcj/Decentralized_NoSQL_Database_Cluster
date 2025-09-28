package org.example.node.service.database;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.node.dto.database.FolderMeta;
import org.example.node.dto.database.DirectoryRenameRequest;
import org.example.node.repository.JsonRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Service
public class DatabaseIndexService {

    private static final Logger logger = LogManager.getLogger(DatabaseIndexService.class);

    private final JsonRepository jsonRepository;
    private final ObjectMapper mapper;

    public DatabaseIndexService(JsonRepository jsonRepository , ObjectMapper mapper) {
        this.jsonRepository = jsonRepository;
        this.mapper = mapper;
    }

    public void storeDatabaseInfo(FolderMeta folderMeta, Path jsonFilePath) throws IOException {
        ObjectNode dbMap = jsonRepository.readIndex(jsonFilePath);
        JsonNode folderMetaNode = mapper.valueToTree(folderMeta);
        dbMap.set(folderMeta.getName(), folderMetaNode);
        jsonRepository.writeIndex(dbMap, jsonFilePath);
        logger.info("Stored database info for '{}' at path {}", folderMeta.getName(), jsonFilePath);
    }

    public void removeDatabaseInfo(String folderName, Path jsonFilePath) throws IOException {
        ObjectNode dbMap = jsonRepository.readIndex(jsonFilePath);
        dbMap.remove(folderName);
        jsonRepository.writeIndex(dbMap, jsonFilePath);
        logger.info("Removed database info for '{}' from path {}", folderName, jsonFilePath);
    }

    public void renameDatabase(DirectoryRenameRequest folderRenameRequest, Path jsonFilePath) throws IOException {
        ObjectNode dbMap = jsonRepository.readIndex(jsonFilePath);

        String oldName = folderRenameRequest.getOldDirectoryName();
        String newName = folderRenameRequest.getNewDirectoryName();

        if (dbMap.has(oldName)) {
            JsonNode folderMetaNode = dbMap.get(oldName);
            FolderMeta folderMeta = mapper.treeToValue(folderMetaNode, FolderMeta.class);
            folderMeta.setName(newName);

            dbMap.remove(oldName);
            JsonNode updatedFolderMetaNode = mapper.valueToTree(folderMeta);
            dbMap.set(newName, updatedFolderMetaNode);
            jsonRepository.writeIndex(dbMap, jsonFilePath);
            logger.info("Renamed database '{}' to '{}' in path {}", oldName, newName, jsonFilePath);
        } else {
            logger.warn("Key '{}' does not exist in path {}", oldName, jsonFilePath);
        }
    }
}
