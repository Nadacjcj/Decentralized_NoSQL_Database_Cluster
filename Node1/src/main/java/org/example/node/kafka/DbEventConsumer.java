package org.example.node.kafka;

import org.example.node.dto.database.DBDeletionRequest;
import org.example.node.dto.database.DirectoryRenameRequest;
import org.example.node.dto.database.DBCreationRequest;
import org.example.node.events.database.DbFolderEvent;
import org.example.node.service.database.DatabaseIndexService;
import org.example.node.service.database.DatabaseManagementService;
import org.example.node.locks.ConsulLockService;
import org.example.node.util.filesystem.PathUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;

@Component
public class DbEventConsumer {

    public static final String DATABASES_INFO_JSON = "databases_info.json";
    public static final String DATABASE_EVENTS = "database-events";
    public static final String $_SPRING_KAFKA_CONSUMER_GROUP_ID = "${spring.kafka.consumer.group-id}";
    @Autowired
    DatabaseManagementService databaseManagementService;

    @Autowired
    DatabaseIndexService databaseIndexService;

    @Autowired
    ConsulLockService lockService;

    @KafkaListener(topics = DATABASE_EVENTS, groupId = $_SPRING_KAFKA_CONSUMER_GROUP_ID)
    public void listen(DbFolderEvent event) throws IOException {
        String userFolder = event.getUserFolderName();
        String dbFolderName = event.getDbFolderName();
        String lockKey = "db-folder_" + userFolder + "_" + dbFolderName;


        boolean lockAcquired = lockService.tryAcquireWithRetry(() -> lockService.acquireWriteLock(lockKey), 10);
        if (!lockAcquired) {
            System.out.println("Could not acquire lock for " + lockKey + " , retrying later.");
            return;
        }

        try {
            Path dbPath = PathUtil.buildPath(userFolder, dbFolderName);

            switch (event.getAction()) {
                case "CREATE":
                    handleCreate(dbPath, userFolder, event);
                    break;

                case "DELETE":
                    handleDelete(dbPath, userFolder, event);
                    break;

                case "RENAME":
                    handleRename(dbPath, userFolder, event);
                    break;
            }
        } finally {
            lockService.releaseWriteLock(lockKey);
        }
    }

    private void handleCreate(Path dbPath, String userFolder, DbFolderEvent event) throws IOException {
        DBCreationRequest request = new DBCreationRequest(event.getDbFolderName(), event.getDbFolderDescription());
        boolean created = databaseManagementService.createFolder(dbPath, userFolder, request);
        if (created) {
            Path metaFile = PathUtil.buildPath(userFolder).resolve(DATABASES_INFO_JSON);
            databaseIndexService.storeDatabaseInfo(event.getFolderMeta(), metaFile);
        }
    }

    private void handleDelete(Path dbPath, String userFolder, DbFolderEvent event) throws IOException {
        DBDeletionRequest request = new DBDeletionRequest(event.getDbFolderName());
        boolean deleted = databaseManagementService.deleteFolder(dbPath, userFolder, request);
        if (deleted) {
            Path metaFile = PathUtil.buildPath(userFolder).resolve(DATABASES_INFO_JSON);
            databaseIndexService.removeDatabaseInfo(request.getDatabaseName(), metaFile);
        }
    }

    private void handleRename(Path dbPath, String userFolder, DbFolderEvent event) throws IOException {
        DirectoryRenameRequest request = new DirectoryRenameRequest(event.getDbFolderName(), event.getDbNewFolderName());
        boolean renamed = databaseManagementService.renameFolder(dbPath, userFolder, request);
        if (renamed) {
            Path metaFile = PathUtil.buildPath(userFolder).resolve(DATABASES_INFO_JSON);
            databaseIndexService.renameDatabase(request, metaFile);
        }
    }
}
