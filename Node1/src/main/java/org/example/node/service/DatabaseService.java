package org.example.node.service;

import org.example.node.dto.*;
import org.example.node.events.DbFolderEvent;
import org.example.node.filter.JwtAuthenticationFilter;
import org.example.node.locks.ConsulLockService;
import org.example.node.util.DirectoryUtil;
import org.example.node.util.PathUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

@Service
public class DatabaseService {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ConsulLockService lockService;

    public String createDatabase(JwtAuthenticationFilter.UserPrincipal user, DBCreationRequest request) throws IOException {
        if (request.getDatabaseName() == null || request.getDatabaseName().isEmpty())
            return "Folder name is required";
        if (request.getDescription() == null)
            request.setDescription("None");

        String userFolder = buildUserFolderName(user);
        String dbFolderName = request.getDatabaseName();
        String lockKey = "db-folder:" + userFolder + ":" + dbFolderName;

        boolean lockAcquired = lockService.tryAcquireWithRetry(() -> lockService.acquireWriteLock(lockKey), 10);
        if (!lockAcquired)
            throw new IllegalStateException("Could not acquire lock for creating database " + dbFolderName);

        try {
            Path dbPath = PathUtil.buildPath(userFolder, dbFolderName);
            DirectoryUtil.createDirectory(dbPath);

            FolderMeta folderMeta = createDbMetaObject(request);

            DbFolderEvent dbFolderEvent = buildEvent(userFolder, dbFolderName, folderMeta, request.getDescription(), "CREATE");
            kafkaTemplate.send("database-events", dbFolderEvent);

            return "Folder created and event published for user: " + userFolder;
        } finally {
            lockService.releaseWriteLock(lockKey);
        }
    }

    public String deleteDatabase(JwtAuthenticationFilter.UserPrincipal user, DBDeletionRequest request) throws IOException {
        if (request.getDatabaseName() == null || request.getDatabaseName().isEmpty())
            return "Cannot delete folder — invalid or missing name";

        String userFolder = buildUserFolderName(user);
        String dbFolderName = request.getDatabaseName();
        String lockKey = "db-folder:" + userFolder + ":" + dbFolderName;

        boolean lockAcquired = lockService.tryAcquireWithRetry(() -> lockService.acquireWriteLock(lockKey), 10);
        if (!lockAcquired)
            throw new IllegalStateException("Could not acquire lock for deleting database " + dbFolderName);

        try {
            DbFolderEvent event = buildEvent(userFolder, dbFolderName, null, null, "DELETE");
            kafkaTemplate.send("database-events", event);
            return "Folder deleted for user: " + userFolder;
        } finally {
            lockService.releaseWriteLock(lockKey);
        }
    }

    public String renameDatabase(JwtAuthenticationFilter.UserPrincipal user, DirectoryRenameRequest request) {
        if (request.getOldDirectoryName().isEmpty() || request.getNewDirectoryName().isEmpty())
            return "Cannot rename folder — invalid or missing name";

        String userFolder = buildUserFolderName(user);
        String lockKey = "db-folder:" + userFolder + ":" + request.getOldDirectoryName();

        boolean lockAcquired = lockService.tryAcquireWithRetry(() -> lockService.acquireWriteLock(lockKey), 10);
        if (!lockAcquired)
            return "Could not acquire lock for renaming folder " + request.getOldDirectoryName();

        try {
            DbFolderEvent event = buildRenameEvent(userFolder, request.getOldDirectoryName(), request.getNewDirectoryName());
            kafkaTemplate.send("database-events", event);
            return "Rename event published for user: " + userFolder;
        } finally {
            lockService.releaseWriteLock(lockKey);
        }
    }

    private FolderMeta createDbMetaObject(DBCreationRequest request) {
        return new FolderMeta(
                UUID.randomUUID().toString(),
                request.getDatabaseName(),
                Instant.now().toString(),
                request.getDescription().equals("None") ? "" : request.getDescription()
        );
    }

    private String buildUserFolderName(JwtAuthenticationFilter.UserPrincipal user) {
        return user.getUsername() + "_" + user.getId();
    }

    private DbFolderEvent buildEvent(String userFolder, String dbFolderName, FolderMeta meta, String description, String action) {
        DbFolderEvent event = new DbFolderEvent();
        event.setUserFolderName(userFolder);
        event.setDbFolderName(dbFolderName);
        event.setFolderMeta(meta);
        event.setDbFolderDescription(description);
        event.setAction(action);
        return event;
    }

    private DbFolderEvent buildRenameEvent(String userFolder, String oldName, String newName) {
        DbFolderEvent event = new DbFolderEvent();
        event.setUserFolderName(userFolder);
        event.setDbFolderName(oldName);
        event.setDbNewFolderName(newName);
        event.setAction("RENAME");
        return event;
    }
}
