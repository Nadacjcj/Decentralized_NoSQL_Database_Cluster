package org.example.node.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.node.filter.JwtAuthenticationFilter;
import org.example.node.repository.JsonRepository;
import org.example.node.util.PathUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

@Service
public class LoadDatabasesService {
    public JsonNode loadDBs(JwtAuthenticationFilter.UserPrincipal user) throws IOException {
           String userFolderName = buildUserFolderName(user);
           Path userFolderPath = PathUtil.buildPath(userFolderName , "databases_info.json");
           return JsonRepository.readDoc(userFolderPath);
    }
    private String buildUserFolderName(JwtAuthenticationFilter.UserPrincipal user) {
        return user.getUsername() + "_" + user.getId();
    }
}
