package org.example.node.kafka;

import org.example.node.events.CollectionEvent;
import org.example.node.events.DocumentEvent;
import org.example.node.locks.ConsulLockService;
import org.example.node.repository.JsonRepository;
import org.example.node.service.DocumentDeletionManager;
import org.example.node.service.DocumentUpdaterService;
import org.example.node.service.IndexUpdaterService;
import org.example.node.service.JsonIndexingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class DocumentEventConsumer {

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

    @KafkaListener(topics = "document-events", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(DocumentEvent event) throws IOException {
        event.process(jsonIndexingService, jsonRepository , documentDeletionManager , documentUpdater , indexUpdater);
    }
}

