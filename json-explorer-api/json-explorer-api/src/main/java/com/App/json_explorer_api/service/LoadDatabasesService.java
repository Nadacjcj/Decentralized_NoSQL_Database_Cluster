package com.App.json_explorer_api.service;

import com.App.json_explorer_api.dto.DatabaseMeta;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
public class LoadDatabasesService {
    private final RestTemplate restTemplate = new RestTemplate();

    public List<DatabaseMeta> loadDatabases(HttpServletRequest request) throws Exception {
        JsonNode databasesMeta = loadDatabasesMeta(request);
        List<DatabaseMeta> dbList = tokeniseJsontoList(databasesMeta);
        return dbList;
    }
    private JsonNode loadDatabasesMeta(HttpServletRequest request){
        String url = "http://localhost:8080/api/database/load";
        String authHeader = request.getHeader("Authorization");

        HttpHeaders headers = new HttpHeaders();
        if (authHeader != null && !authHeader.isEmpty()) {
            headers.set("Authorization", authHeader);
        }

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<JsonNode> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                JsonNode.class
        );

        return response.getBody();
    }
    private List<DatabaseMeta> tokeniseJsontoList(JsonNode databasesMeta) {
        List<DatabaseMeta> dbList = new ArrayList<>();

        if (databasesMeta.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = databasesMeta.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                JsonNode dbNode = entry.getValue();
                DatabaseMeta dto = new DatabaseMeta(
                        dbNode.get("id").asText(),
                        dbNode.get("name").asText(),
                        dbNode.get("description").asText()
                );
                dbList.add(dto);
            }
        }
        return dbList;
    }
}
