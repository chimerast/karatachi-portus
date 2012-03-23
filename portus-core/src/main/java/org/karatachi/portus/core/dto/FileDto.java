package org.karatachi.portus.core.dto;

import java.io.Serializable;
import java.util.Date;

public class FileDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;

    private boolean publish;
    private boolean authorize;
    private boolean streaming;

    private Date openDate;
    private Date closeDate;

    private boolean directory;

    private long size;
    private String digest;

    private String fullPath;
    private boolean actualPublished;
    private boolean actualAuthorized;
    private Date actualOpenDate;
    private Date actualCloseDate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPublish() {
        return publish;
    }

    public void setPublish(boolean publish) {
        this.publish = publish;
    }

    public boolean isAuthorize() {
        return authorize;
    }

    public void setAuthorize(boolean authorize) {
        this.authorize = authorize;
    }

    public boolean isStreaming() {
        return streaming;
    }

    public void setStreaming(boolean streaming) {
        this.streaming = streaming;
    }

    public Date getOpenDate() {
        return openDate;
    }

    public void setOpenDate(Date openDate) {
        this.openDate = openDate;
    }

    public Date getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(Date closeDate) {
        this.closeDate = closeDate;
    }

    public boolean isDirectory() {
        return directory;
    }

    public void setDirectory(boolean directory) {
        this.directory = directory;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    public boolean isActualPublished() {
        return actualPublished;
    }

    public void setActualPublished(boolean actualPublished) {
        this.actualPublished = actualPublished;
    }

    public boolean isActualAuthorized() {
        return actualAuthorized;
    }

    public void setActualAuthorized(boolean actualAuthorized) {
        this.actualAuthorized = actualAuthorized;
    }

    public Date getActualOpenDate() {
        return actualOpenDate;
    }

    public void setActualOpenDate(Date actualOpenDate) {
        this.actualOpenDate = actualOpenDate;
    }

    public Date getActualCloseDate() {
        return actualCloseDate;
    }

    public void setActualCloseDate(Date actualCloseDate) {
        this.actualCloseDate = actualCloseDate;
    }
}
