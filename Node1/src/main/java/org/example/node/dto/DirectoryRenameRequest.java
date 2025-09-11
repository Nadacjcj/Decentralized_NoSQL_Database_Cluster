package org.example.node.dto;

import java.io.Serializable;

public class DirectoryRenameRequest implements Serializable {
    private  String oldDirectoryName;
    private  String newDirectoryName;

    public DirectoryRenameRequest(){}
    public DirectoryRenameRequest(String oldDirectoryName, String newDirectoryName) {
        this.oldDirectoryName = oldDirectoryName;
        this.newDirectoryName = newDirectoryName;
    }
    public String getNewDirectoryName() {
        return newDirectoryName;
    }
    public String getOldDirectoryName() {
        return oldDirectoryName;
    }

    public void setOldDirectoryName(String oldDirectoryName) {
        this.oldDirectoryName = oldDirectoryName;
    }

    public void setNewDirectoryName(String newDirectoryName) {
        this.newDirectoryName = newDirectoryName;
    }
}
