package org.karatachi.portus.api.type;

import java.io.Serializable;

public class CreateDirectoryResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private String created;

    public CreateDirectoryResponse() {
    }

    public CreateDirectoryResponse(String created) {
        this.created = created;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }
}
