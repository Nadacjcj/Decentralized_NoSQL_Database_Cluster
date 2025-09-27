package org.example.node.events.database;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.node.dto.database.FolderMeta;

import java.io.Serializable;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DbFolderEvent implements Serializable {

   private String userFolderName;
   private String dbFolderName;
   private String dbNewFolderName;
   private String dbFolderDescription;
   private String action;
   private FolderMeta folderMeta;

}
