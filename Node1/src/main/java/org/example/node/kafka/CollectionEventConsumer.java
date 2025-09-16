package org.example.node.kafka;

import org.example.node.events.CollectionEvent;
import org.example.node.service.CollectionIndexService;
import org.example.node.service.CollectionManagementService;
import org.example.node.service.JsonIndexingService;
import org.example.node.util.PathUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

@Component
public class CollectionEventConsumer {

    @Autowired
    private CollectionManagementService collectionManagementService;

    @Autowired
    private CollectionIndexService collectionIndexService;

    @Autowired
    private JsonIndexingService jsonIndexingService;

    @KafkaListener(topics = "collection-events", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(CollectionEvent event) throws IOException {
        event.process(collectionManagementService, collectionIndexService, jsonIndexingService);
    }
}
