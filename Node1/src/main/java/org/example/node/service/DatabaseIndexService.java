package org.example.node.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.node.dto.FolderMeta;
import org.example.node.dto.DirectoryRenameRequest;
import org.example.node.repository.JsonRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;


// this updates the databases_info.json some sort of an indexing by "name" file
@Service
public class DatabaseIndexService {
    private final JsonRepository jsonRepository;
    private final ObjectMapper mapper =  new ObjectMapper();

    public DatabaseIndexService(JsonRepository jsonRepository) {
        this.jsonRepository = jsonRepository;
    }

    public void storeDatabaseInfo(FolderMeta folderMeta, Path jsonFilePath) throws IOException {
        ObjectNode dbMap = jsonRepository.readIndex(jsonFilePath);
        JsonNode folderMetaNode = mapper.valueToTree(folderMeta);
        dbMap.set(folderMeta.getName(), folderMetaNode);
        jsonRepository.writeIndex(dbMap, jsonFilePath);
    }

    public void removeDatabaseInfo(String folderName, Path jsonFilePath) throws IOException {
        ObjectNode dbMap = jsonRepository.readIndex(jsonFilePath);
        dbMap.remove(folderName);
        jsonRepository.writeIndex(dbMap, jsonFilePath);
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
        } else {
            System.out.println("Key '" + oldName + "' doesn't exist");
        }
    }


}

