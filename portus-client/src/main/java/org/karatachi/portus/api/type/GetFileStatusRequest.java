package org.karatachi.portus.api.type;

import java.io.Serializable;

public class GetFileStatusRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String path;
    private long idx;

    public GetFileStatusRequest() {
    }

    public GetFileStatusRequest(String path, long idx) {
        this.path = path;
        this.idx = idx;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getIdx() {
        return idx;
    }

    public void setIdx(long idx) {
        this.idx = idx;
    }
}
