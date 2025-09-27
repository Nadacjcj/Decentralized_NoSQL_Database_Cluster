package org.example.node.dto.database;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DirectoryRenameRequest implements Serializable {
    private  String oldDirectoryName;
    private  String newDirectoryName;

    public DirectoryRenameRequest(String oldDirectoryName, String newDirectoryName) {
        this.oldDirectoryName = oldDirectoryName;
        this.newDirectoryName = newDirectoryName;
    }

}
