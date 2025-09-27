package org.example.node.kafka;

import org.example.node.events.collection.CollectionEvent;
import org.example.node.service.collection.CollectionIndexService;
import org.example.node.service.collection.CollectionManagementService;
import org.example.node.service.indexing.JsonIndexingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CollectionEventConsumer {

    public static final String COLLECTION_EVENTS = "collection-events";
    public static final String $_SPRING_KAFKA_CONSUMER_GROUP_ID = "${spring.kafka.consumer.group-id}";
    @Autowired
    private CollectionManagementService collectionManagementService;

    @Autowired
    private CollectionIndexService collectionIndexService;

    @Autowired
    private JsonIndexingService jsonIndexingService;

    @KafkaListener(topics = COLLECTION_EVENTS, groupId = $_SPRING_KAFKA_CONSUMER_GROUP_ID)
    public void listen(CollectionEvent event) throws IOException {
        event.process(collectionManagementService, collectionIndexService, jsonIndexingService);
    }
}
