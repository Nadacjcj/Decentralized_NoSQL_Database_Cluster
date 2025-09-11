package org.example.node.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.node.dto.FindQueryRequest;
import org.example.node.filter.JwtAuthenticationFilter;
import org.example.node.repository.JsonRepository;
import org.example.node.util.JsonFileLister;
import org.example.node.util.PathUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@Service
public class FindQueryService {
    @Autowired
    private FilteringService filteringService;

    @Autowired
    private JsonRepository jsonRepository;

    public List<JsonNode> find(JwtAuthenticationFilter.UserPrincipal user, String databaseName, String collectionName , FindQueryRequest findQueryRequest) throws IOException {
        Objects.requireNonNull(findQueryRequest, "FindQueryRequest must not be null");
        String userFolder = buildUserFolderName(user);

        Path collectionPath = PathUtil.buildPath(userFolder, databaseName, collectionName);
        Path schemaPath = collectionPath.resolve("schema.json");

        JsonNode flattenedSchema = jsonRepository.readIndex(schemaPath);

        JsonNode filter = findQueryRequest.getFilter();
        if (filter == null || filter.isEmpty()) {
            return findAll(collectionPath);
        }

        HashSet<String> matchingDocIds = filteringService.applyFilter(filter, flattenedSchema, collectionPath);
        return JsonFileLister.listDocsContent(collectionPath, matchingDocIds);
    }

    public List<JsonNode> findAll(Path collectionPath) throws IOException {
        return JsonFileLister.listAllJsonDocsContent(collectionPath);
    }
    private String buildUserFolderName(JwtAuthenticationFilter.UserPrincipal user){
        return user.getUsername() + "_" + user.getId();

    }
}
