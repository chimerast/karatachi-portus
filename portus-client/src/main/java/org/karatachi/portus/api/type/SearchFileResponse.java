package org.karatachi.portus.api.type;

import java.io.Serializable;

public class SearchFileResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private String path;
    private long registered;

    public SearchFileResponse() {
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getRegistered() {
        return registered;
    }

    public void setRegistered(long registered) {
        this.registered = registered;
    }
}
