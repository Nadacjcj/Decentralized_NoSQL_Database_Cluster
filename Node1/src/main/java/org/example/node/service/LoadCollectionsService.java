package org.example.node.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.node.filter.JwtAuthenticationFilter;
import org.example.node.repository.JsonRepository;
import org.example.node.util.PathUtil;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

@Service
public class LoadCollectionsService {

    public JsonNode loadCollections(JwtAuthenticationFilter.UserPrincipal user, JsonNode DBName) throws IOException {
        String userFolderName = buildDBFolderName(user);
        String databaseName = DBName.get("databaseName").asText();

        Path userFolderPath = PathUtil.buildPath(userFolderName , databaseName , "collections_info.json");
        return JsonRepository.readDoc(userFolderPath);
    }
    private String buildDBFolderName(JwtAuthenticationFilter.UserPrincipal user) {
        return user.getUsername() + "_" + user.getId();
    }
}
