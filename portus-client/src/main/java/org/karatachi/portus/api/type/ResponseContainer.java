package org.karatachi.portus.api.type;

import java.io.Serializable;

public class ResponseContainer implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private int code;
    private Object response;

    public ResponseContainer() {
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Object getResponse() {
        return response;
    }

    public void setResponse(Object response) {
        this.response = response;
    }
}
