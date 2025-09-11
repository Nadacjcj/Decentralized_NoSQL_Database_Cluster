package org.example.node.service;
import org.example.node.dto.*;
import org.example.node.dto.DBCreationRequest;
import org.example.node.events.DbFolderEvent;
import org.example.node.filter.JwtAuthenticationFilter;
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

        public String createDatabase(JwtAuthenticationFilter.UserPrincipal user , DBCreationRequest request) throws IOException {
            if (request.getDatabaseName() == null || request.getDatabaseName().isEmpty()) {
                return "Folder name is required";
            }
            if (request.getDescription() == null) {
                request.setDescription("None");
            }

            try {
                String userFolder = buildUserFolderName(user);
                FolderMeta folderMeta = createDbMetaObject(request);

                DbFolderEvent dbFolderEvent = new DbFolderEvent();
                dbFolderEvent.setUserFolderName(userFolder);
                dbFolderEvent.setDbFolderName(request.getDatabaseName());
                dbFolderEvent.setFolderMeta(folderMeta);
                dbFolderEvent.setDbFolderDescription(request.getDescription());
                dbFolderEvent.setAction("CREATE");
                kafkaTemplate.send("database-events", dbFolderEvent);


                return "Folder created for user: " + userFolder + " ** " + dbFolderEvent;
            }
            catch (Exception e){
                return "Folder creation failed for user: " + user.getUsername();
            }
        }


    public String deleteDatabase(JwtAuthenticationFilter.UserPrincipal user , DBDeletionRequest folderRequest) throws IOException {
        DbFolderEvent dbFolderEvent = new DbFolderEvent();
        try {
            String userFolder = buildUserFolderName(user);
            if (folderRequest.getDatabaseName() == null || folderRequest.getDatabaseName().isEmpty()) {
                return "Cant Rename folder , either an Invalid / replicated name / Missing Description";
            }
            dbFolderEvent.setUserFolderName(userFolder);
            dbFolderEvent.setDbFolderName(folderRequest.getDatabaseName());
            dbFolderEvent.setAction("DELETE");
            kafkaTemplate.send("database-events", dbFolderEvent);

            return "Folder deleted for user: " + dbFolderEvent;
        }
        catch (Exception e){
            return "Folder deletion failed for user: " + dbFolderEvent;
        }
    }


    public String renameDatabase(JwtAuthenticationFilter.UserPrincipal user, DirectoryRenameRequest request) {

        try {
            String userFolder = buildUserFolderName(user);

            if (request.getOldDirectoryName().isEmpty() || request.getNewDirectoryName().isEmpty()) {
                return "Cant Rename folder , either an Invalid / replicated name";
            }

            DbFolderEvent dbFolderEvent = new DbFolderEvent();
            dbFolderEvent.setUserFolderName(userFolder);
            dbFolderEvent.setDbFolderName(request.getOldDirectoryName());
            dbFolderEvent.setDbNewFolderName(request.getNewDirectoryName());
            dbFolderEvent.setAction("RENAME");

            kafkaTemplate.send("database-events", dbFolderEvent);

            return "Rename event published for user: " + userFolder;
        }
        catch (Exception e){
            return "Folder rename failed for user: " + user.getUsername();
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
    private String buildUserFolderName(JwtAuthenticationFilter.UserPrincipal user){
        return user.getUsername() + "_" + user.getId();

    }


}
