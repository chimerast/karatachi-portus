package org.karatachi.portus.api.type;

import java.io.Serializable;

public class GetFileStatusResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private long idx;
    private long status;

    public GetFileStatusResponse() {
    }

    public long getIdx() {
        return idx;
    }

    public void setIdx(long idx) {
        this.idx = idx;
    }

    public long getStatus() {
        return status;
    }

    public void setStatus(long status) {
        this.status = status;
    }
}
