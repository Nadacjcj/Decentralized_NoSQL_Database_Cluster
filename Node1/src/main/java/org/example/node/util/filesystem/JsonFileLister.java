package org.example.node.util.filesystem;


import com.fasterxml.jackson.databind.JsonNode;
import org.example.node.repository.JsonRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JsonFileLister {

    public static final String DOCUMENTS = "documents";
    public static final String JSON = ".json";
    @Autowired
    private JsonRepository jsonRepository;

    public static HashSet<Path> listJsonFilePaths(Path dirPath) throws IOException {
        try (Stream<Path> paths = Files.list(dirPath)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(JSON))
                    .collect(Collectors.toCollection(HashSet::new));
        }
    }
    public static HashSet<String> listJsonFileNames(Path dirPath) throws IOException {
        try (Stream<Path> paths = Files.list(dirPath)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(JSON))
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.toCollection(HashSet::new));
        }
    }
    public static List<JsonNode> listAllJsonDocsContent(Path collectionPath) throws IOException {
        HashSet<Path> docsPaths = listJsonFilePaths(collectionPath.resolve(DOCUMENTS));
        List<JsonNode> docsContent = new ArrayList<>();
        for(Path path : docsPaths) {
            docsContent.add(JsonRepository.readDoc(path));
        }
        return docsContent;
    }
    public static List<JsonNode> listDocsContent(Path collectionPath , HashSet<String> docsId) throws IOException {
        Path docsPaths = collectionPath.resolve(DOCUMENTS);

        List<JsonNode> docsContent = new ArrayList<>();
        for (String docId : docsId) {
            System.out.println("Docs path  " + toDocumentFile(docId));
            System.out.println((docsPaths.resolve(toDocumentFile(docId))));
            docsContent.add(JsonRepository.readIndex(docsPaths.resolve(toDocumentFile(docId))));
        }
        return docsContent;
    }
    private static String toDocumentFile(String docId) {
        return docId + JSON;
    }


}
