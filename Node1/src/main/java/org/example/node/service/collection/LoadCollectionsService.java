package org.example.node.service.collection;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.node.filter.JwtAuthenticationFilter;
import org.example.node.repository.JsonRepository;
import org.example.node.util.filesystem.PathUtil;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

@Service
public class LoadCollectionsService {

    public static final String DATABASE_NAME = "databaseName";
    public static final String COLLECTIONS_INFO_JSON = "collections_info.json";
    public static final String SCHEMA_JSON = "schema.json";

    public JsonNode loadCollections(JwtAuthenticationFilter.UserPrincipal user, JsonNode DBName) throws IOException {
        String userFolderName = buildDBFolderName(user);
        String databaseName = DBName.get(DATABASE_NAME).asText();

        Path userFolderPath = PathUtil.buildPath(userFolderName , databaseName , COLLECTIONS_INFO_JSON);
        return JsonRepository.readDoc(userFolderPath);
    }
    public JsonNode loadSchema(JwtAuthenticationFilter.UserPrincipal user, String databaseName , String collectionName) throws IOException {
        String userFolderName = buildDBFolderName(user);

        Path userFolderPath = PathUtil.buildPath(userFolderName , databaseName , collectionName).resolve(SCHEMA_JSON);
        return JsonRepository.readDoc(userFolderPath);
    }
    private String buildDBFolderName(JwtAuthenticationFilter.UserPrincipal user) {
        return user.getUsername() + "_" + user.getId();
    }
}
