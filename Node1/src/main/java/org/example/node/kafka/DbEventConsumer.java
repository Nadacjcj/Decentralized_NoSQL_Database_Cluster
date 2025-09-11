package org.example.node.kafka;

import org.example.node.dto.DBDeletionRequest;
import org.example.node.dto.DirectoryRenameRequest;
import org.example.node.dto.DBCreationRequest;
import org.example.node.dto.FolderMeta;
import org.example.node.events.DbFolderEvent;
import org.example.node.service.DatabaseIndexService;
import org.example.node.service.DatabaseManagementService;
import org.example.node.util.DirectoryUtil;
import org.example.node.util.PathUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

@Component
public class DbEventConsumer {
    @Autowired
    DatabaseManagementService databaseManagementService;

    @Autowired
    DatabaseIndexService databaseIndexService;
    public DbEventConsumer(){}

    @KafkaListener(topics = "database-events", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(DbFolderEvent event) throws IOException {
        Path dbPath = PathUtil.buildPath(event.getUserFolderName(), event.getDbFolderName());

        switch (event.getAction()) {
            case "CREATE":
                DBCreationRequest createFolderRequest = new DBCreationRequest(event.getDbFolderName() , event.getDbFolderDescription());
                boolean canCreateDB = databaseManagementService.createFolder(dbPath , event.getUserFolderName(),createFolderRequest);
                if(canCreateDB) {
                    Path metaFilePath = PathUtil.buildPath(event.getUserFolderName()).resolve("databases_info.json");
                    databaseIndexService.storeDatabaseInfo(event.getFolderMeta(), metaFilePath);
                }

                break;
            case "DELETE":
                DBDeletionRequest deleteFolderRequest = new DBDeletionRequest(event.getDbFolderName());
                boolean canDeleteDB =  databaseManagementService.deleteFolder(dbPath , event.getUserFolderName(), deleteFolderRequest);
                if(canDeleteDB){
                    Path metaFilePath = PathUtil.buildPath(event.getUserFolderName()).resolve("databases_info.json");
                    databaseIndexService.removeDatabaseInfo(deleteFolderRequest.getDatabaseName(), metaFilePath);
                }

                break;
            case "RENAME":
                DirectoryRenameRequest renameRequest =
                        new DirectoryRenameRequest(event.getDbFolderName(), event.getDbNewFolderName());

                boolean canRenameDB = databaseManagementService.renameFolder(dbPath, event.getUserFolderName(), renameRequest);

                if (canRenameDB) {
                    Path metaFilePath = PathUtil.buildPath(event.getUserFolderName()).resolve("databases_info.json");
                    databaseIndexService.renameDatabase(renameRequest, metaFilePath);
                }
                break;

        }
    }
}
