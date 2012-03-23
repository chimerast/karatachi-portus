package org.karatachi.portus.core.dto;

import java.io.Serializable;

public class NodeFileDto implements Serializable, Comparable<NodeFileDto> {
    private static final long serialVersionUID = 1L;

    private long id;

    private long fileId;
    private long nodeId;
    private long fromNodeId;

    private String name;

    private String filename;
    private long size;
    private String digest;

    private String fullPath;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getFileId() {
        return fileId;
    }

    public void setFileId(long fileId) {
        this.fileId = fileId;
    }

    public long getNodeId() {
        return nodeId;
    }

    public void setNodeId(long nodeId) {
        this.nodeId = nodeId;
    }

    public long getFromNodeId() {
        return fromNodeId;
    }

    public void setFromNodeId(long fromNodeId) {
        this.fromNodeId = fromNodeId;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != NodeFileDto.class) {
            return false;
        }
        NodeFileDto o = (NodeFileDto) obj;
        return this.filename.equals(o.filename);
    }

    @Override
    public int compareTo(NodeFileDto o) {
        return this.filename.compareTo(o.filename);
    }
}
