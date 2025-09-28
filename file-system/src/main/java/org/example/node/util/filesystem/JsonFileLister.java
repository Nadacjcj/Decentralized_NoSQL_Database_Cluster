package org.example.node.util.filesystem;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    private static final Logger logger = LogManager.getLogger(JsonFileLister.class);

    public static final String DOCUMENTS = "documents";
    public static final String JSON = ".json";

    @Autowired
    private JsonRepository jsonRepository;

    public static HashSet<Path> listJsonFilePaths(Path dirPath) throws IOException {
        try (Stream<Path> paths = Files.list(dirPath)) {
            HashSet<Path> result = paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(JSON))
                    .collect(Collectors.toCollection(HashSet::new));
            logger.debug("Listed JSON file paths in {}: {}", dirPath, result);
            return result;
        }
    }

    public static HashSet<String> listJsonFileNames(Path dirPath) throws IOException {
        try (Stream<Path> paths = Files.list(dirPath)) {
            HashSet<String> result = paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(JSON))
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.toCollection(HashSet::new));
            logger.debug("Listed JSON file names in {}: {}", dirPath, result);
            return result;
        }
    }

    public static List<JsonNode> listAllJsonDocsContent(Path collectionPath) throws IOException {
        HashSet<Path> docsPaths = listJsonFilePaths(collectionPath.resolve(DOCUMENTS));
        List<JsonNode> docsContent = new ArrayList<>();
        for (Path path : docsPaths) {
            docsContent.add(JsonRepository.readDoc(path));
        }
        logger.debug("Loaded content of all JSON docs in {}: {} files", collectionPath, docsPaths.size());
        return docsContent;
    }

    public static List<JsonNode> listDocsContent(Path collectionPath, HashSet<String> docsId) throws IOException {
        Path docsPaths = collectionPath.resolve(DOCUMENTS);
        List<JsonNode> docsContent = new ArrayList<>();
        for (String docId : docsId) {
            Path docPath = docsPaths.resolve(toDocumentFile(docId));
            logger.debug("Loading document: {}", docPath);
            docsContent.add(JsonRepository.readIndex(docPath));
        }
        return docsContent;
    }

    private static String toDocumentFile(String docId) {
        return docId + JSON;
    }
}
