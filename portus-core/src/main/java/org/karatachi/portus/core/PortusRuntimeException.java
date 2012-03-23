package org.karatachi.portus.core;

public class PortusRuntimeException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final int code;

    public PortusRuntimeException(String message, Throwable cause, int code) {
        super(message, cause);
        this.code = code;
    }

    public PortusRuntimeException(String message, int code) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
