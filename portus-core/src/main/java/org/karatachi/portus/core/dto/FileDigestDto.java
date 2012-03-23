package org.karatachi.portus.core.dto;

import java.io.Serializable;

public class FileDigestDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private long size;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
