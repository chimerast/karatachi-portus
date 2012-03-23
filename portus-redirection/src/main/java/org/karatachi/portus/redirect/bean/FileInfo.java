package org.karatachi.portus.redirect.bean;

public class FileInfo {
    private final long id;
    private final long domainId;
    private final boolean directory;
    private final String name;
    private final boolean authorized;
    private final String referer;
    private final int replication;
    private final long size;
    private final long fileTypeId;
    private final String allowFrom;
    private final String fullPath;

    public FileInfo(long id, long domainId, boolean directory, String name,
            boolean authorized, String referer, int replication, long size,
            long fileTypeId, String allowFrom, String fullPath) {
        this.id = id;
        this.domainId = domainId;
        this.directory = directory;
        this.name = name;
        this.authorized = authorized;
        this.referer = referer;
        this.replication = replication;
        this.size = size;
        this.fileTypeId = fileTypeId;
        this.allowFrom = allowFrom;
        this.fullPath = fullPath;
    }

    public long getId() {
        return id;
    }

    public long getDomainId() {
        return domainId;
    }

    public boolean isDirectory() {
        return directory;
    }

    public String getName() {
        return name;
    }

    public boolean isAuthorized() {
        return authorized;
    }

    public String getReferer() {
        return referer;
    }

    public int getReplication() {
        return replication;
    }

    public long getSize() {
        return size;
    }

    public String getAllowFrom() {
        return allowFrom;
    }

    public String getFullPath() {
        return fullPath;
    }

    public long getFileTypeId() {
        return fileTypeId;
    }
}
