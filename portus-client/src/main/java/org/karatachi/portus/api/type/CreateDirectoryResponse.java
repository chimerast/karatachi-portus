package org.karatachi.portus.api.type;

import java.io.Serializable;

public class CreateDirectoryResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private String createdDirectory;

    public CreateDirectoryResponse(String createdDirectory) {
        this.createdDirectory = createdDirectory;
    }

    public String getCreatedDirectory() {
        return createdDirectory;
    }

    public void setCreatedDirectory(String createdDirectory) {
        this.createdDirectory = createdDirectory;
    }
}
