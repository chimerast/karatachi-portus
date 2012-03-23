package org.karatachi.portus.redirect.bean;

public class NodeInfo {
    private final long id;
    private final String ipAddress;
    private final int port;
    private final int revision;

    public NodeInfo(long id, String ipAddress, int port, int revision) {
        super();
        this.id = id;
        this.ipAddress = ipAddress;
        this.port = port;
        this.revision = revision;
    }

    public long getId() {
        return id;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getPort() {
        return port;
    }

    public int getRevision() {
        return revision;
    }
}
