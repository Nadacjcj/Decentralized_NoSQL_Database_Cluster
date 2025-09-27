package org.example.node.service.database;

import org.example.node.dto.database.DBCreationRequest;
import org.example.node.dto.database.DBDeletionRequest;
import org.example.node.dto.database.DirectoryRenameRequest;
import org.example.node.dto.database.FolderMeta;
import org.example.node.events.database.DbFolderEvent;
import org.example.node.filter.JwtAuthenticationFilter;
import org.example.node.locks.ConsulLockService;
import org.example.node.util.filesystem.DirectoryUtil;
import org.example.node.util.filesystem.PathUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

@Service
public class DatabaseService {

    public static final String COULD_NOT_ACQUIRE_LOCK_FOR_CREATING_DATABASE = "Could not acquire lock for creating database ";
    public static final String DATABASE_EVENTS = "database-events";
    public static final String FOLDER_NAME_IS_REQUIRED = "Folder name is required";
    public static final String CANNOT_DELETE_FOLDER_INVALID_OR_MISSING_NAME = "Cannot delete folder — invalid or missing name";
    public static final String COULD_NOT_ACQUIRE_LOCK_FOR_DELETING_DATABASE = "Could not acquire lock for deleting database ";
    public static final String CANNOT_RENAME_FOLDER_INVALID_OR_MISSING_NAME = "Cannot rename folder — invalid or missing name";
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ConsulLockService lockService;

    public String createDatabase(JwtAuthenticationFilter.UserPrincipal user, DBCreationRequest request) throws IOException {
        if (request.getDatabaseName() == null || request.getDatabaseName().isEmpty())
            return FOLDER_NAME_IS_REQUIRED;
        if (request.getDescription() == null)
            request.setDescription("None");

        String userFolder = buildUserFolderName(user);
        String dbFolderName = request.getDatabaseName();
        String lockKey = "db-folder_" + userFolder + "_" + dbFolderName;

        System.out.println("ALOOOOOOOOOOO "+ lockKey + "***************************");
        boolean lockAcquired = lockService.tryAcquireWithRetry(() -> lockService.acquireWriteLock(lockKey), 10);
        if (!lockAcquired)
            throw new IllegalStateException(COULD_NOT_ACQUIRE_LOCK_FOR_CREATING_DATABASE + dbFolderName);

        try {
            Path dbPath = PathUtil.buildPath(userFolder, dbFolderName);
            DirectoryUtil.createDirectory(dbPath);

            FolderMeta folderMeta = createDbMetaObject(request);

            DbFolderEvent dbFolderEvent = buildEvent(userFolder, dbFolderName, folderMeta, request.getDescription(), "CREATE");
            kafkaTemplate.send(DATABASE_EVENTS, dbFolderEvent);

            return "Folder created and event published for user: " + userFolder;
        } finally {
            lockService.releaseWriteLock(lockKey);
        }
    }

    public String deleteDatabase(JwtAuthenticationFilter.UserPrincipal user, DBDeletionRequest request) throws IOException {
        if (request.getDatabaseName() == null || request.getDatabaseName().isEmpty())
            return CANNOT_DELETE_FOLDER_INVALID_OR_MISSING_NAME;

        String userFolder = buildUserFolderName(user);
        String dbFolderName = request.getDatabaseName();
        String lockKey = "db-folder_" + userFolder + "_" + dbFolderName;

        boolean lockAcquired = lockService.tryAcquireWithRetry(() -> lockService.acquireWriteLock(lockKey), 10);
        if (!lockAcquired)
            throw new IllegalStateException(COULD_NOT_ACQUIRE_LOCK_FOR_DELETING_DATABASE + dbFolderName);

        try {
            DbFolderEvent event = buildEvent(userFolder, dbFolderName, null, null, "DELETE");
            kafkaTemplate.send(DATABASE_EVENTS, event);
            return "Folder deleted for user: " + userFolder;
        } finally {
            lockService.releaseWriteLock(lockKey);
        }
    }

    public String renameDatabase(JwtAuthenticationFilter.UserPrincipal user, DirectoryRenameRequest request) {
        if (request.getOldDirectoryName().isEmpty() || request.getNewDirectoryName().isEmpty())
            return CANNOT_RENAME_FOLDER_INVALID_OR_MISSING_NAME;

        String userFolder = buildUserFolderName(user);
        String lockKey = "db-folder_" + userFolder + "_" + request.getOldDirectoryName();

        boolean lockAcquired = lockService.tryAcquireWithRetry(() -> lockService.acquireWriteLock(lockKey), 10);
        if (!lockAcquired)
            return "Could not acquire lock for renaming folder " + request.getOldDirectoryName();

        try {
            DbFolderEvent event = buildRenameEvent(userFolder, request.getOldDirectoryName(), request.getNewDirectoryName());
            kafkaTemplate.send(DATABASE_EVENTS, event);
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
