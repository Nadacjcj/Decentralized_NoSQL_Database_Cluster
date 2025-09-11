package org.example.node.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.example.node.dto.FolderMeta;
import org.example.node.dto.DBCreationRequest;

import java.io.Serializable;
import java.nio.file.Path;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DbFolderEvent implements Serializable {

   private String userFolderName;
   private String dbFolderName;
   private String dbNewFolderName;
   private String dbFolderDescription;
   private String action;
   private FolderMeta folderMeta;

   public DbFolderEvent(){

   }
    public String getUserFolderName() {
        return userFolderName;
    }

    public void setUserFolderName(String userFolderName) {
        this.userFolderName = userFolderName;
    }

    public void setDbFolderName(String dbFolderName) {
        this.dbFolderName = dbFolderName;
    }

    public String getDbFolderName() {
        return dbFolderName;
    }

    public String getDbFolderDescription() {
        return dbFolderDescription;
    }

    public void setDbFolderDescription(String dbFolderDescription) {
        this.dbFolderDescription = dbFolderDescription;
    }
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDbNewFolderName() {
        return dbNewFolderName;
    }

    public void setDbNewFolderName(String dbNewFolderName) {
        this.dbNewFolderName = dbNewFolderName;
    }

    public FolderMeta getFolderMeta() {
        return folderMeta;
    }

    public void setFolderMeta(FolderMeta folderMeta) {
        this.folderMeta = folderMeta;
    }

    @Override
    public String toString() {
        return "DbFolderEvent{" +
                "userFolderName='" + userFolderName + '\'' +
                ", dbFolderName='" + dbFolderName + '\'' +
                ", dbNewFolderName='" + dbNewFolderName + '\'' +
                ", dbFolderDescription='" + dbFolderDescription + '\'' +
                ", action='" + action + '\'' +
                ", folderMeta=" + folderMeta +
                '}';
    }
}
