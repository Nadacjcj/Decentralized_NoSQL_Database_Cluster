package org.example.node.kafka;

import org.example.node.events.document.DocumentEvent;
import org.example.node.locks.ConsulLockService;
import org.example.node.repository.JsonRepository;
import org.example.node.service.queries.DocumentDeletionManager;
import org.example.node.service.queries.DocumentUpdaterService;
import org.example.node.service.indexing.IndexUpdaterService;
import org.example.node.service.indexing.JsonIndexingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class DocumentEventConsumer {

    public static final String DOCUMENT_EVENTS = "document-events";
    public static final String $_SPRING_KAFKA_CONSUMER_GROUP_ID = "${spring.kafka.consumer.group-id}";
    @Autowired
    private JsonIndexingService jsonIndexingService;
    @Autowired
    private JsonRepository jsonRepository;
    @Autowired
    private DocumentDeletionManager documentDeletionManager;
    @Autowired
    private  DocumentUpdaterService documentUpdater;
    @Autowired
    private  IndexUpdaterService indexUpdater;
    @Autowired
    ConsulLockService lockService;

    @KafkaListener(topics = DOCUMENT_EVENTS, groupId = $_SPRING_KAFKA_CONSUMER_GROUP_ID)
    public void listen(DocumentEvent event) throws IOException {
        event.process(jsonIndexingService, jsonRepository , documentDeletionManager , documentUpdater , indexUpdater);
    }
}

