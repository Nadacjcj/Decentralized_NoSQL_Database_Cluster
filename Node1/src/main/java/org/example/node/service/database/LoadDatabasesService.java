package org.example.node.service.database;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.node.filter.JwtAuthenticationFilter;
import org.example.node.repository.JsonRepository;
import org.example.node.util.filesystem.PathUtil;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

@Service
public class LoadDatabasesService {

    public static final String DATABASES_INFO_JSON = "databases_info.json";

    public JsonNode loadDBs(JwtAuthenticationFilter.UserPrincipal user) throws IOException {
           String userFolderName = buildUserFolderName(user);
           Path userFolderPath = PathUtil.buildPath(userFolderName , DATABASES_INFO_JSON);
           return JsonRepository.readDoc(userFolderPath);
    }
    private String buildUserFolderName(JwtAuthenticationFilter.UserPrincipal user) {
        return user.getUsername() + "_" + user.getId();
    }
}
