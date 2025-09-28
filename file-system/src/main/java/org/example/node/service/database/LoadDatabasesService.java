package org.example.node.service.database;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.node.filter.JwtAuthenticationFilter;
import org.example.node.repository.JsonRepository;
import org.example.node.util.filesystem.PathUtil;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Service
public class LoadDatabasesService {

    private static final Logger logger = LogManager.getLogger(LoadDatabasesService.class);

    public static final String DATABASES_INFO_JSON = "databases_info.json";

    public JsonNode loadDBs(JwtAuthenticationFilter.UserPrincipal user) throws IOException {
        String userFolderName = buildUserFolderName(user);
        Path userFolderPath = PathUtil.buildPath(userFolderName, DATABASES_INFO_JSON);
        JsonNode result = JsonRepository.readDoc(userFolderPath);
        logger.info("Loaded databases info for user '{}' from path {}", userFolderName, userFolderPath);
        return result;
    }

    private String buildUserFolderName(JwtAuthenticationFilter.UserPrincipal user) {
        return user.getUsername() + "_" + user.getId();
    }
}
