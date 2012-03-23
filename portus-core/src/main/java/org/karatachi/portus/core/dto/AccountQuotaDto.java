package org.karatachi.portus.core.dto;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;

public class AccountQuotaDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private long usedSpace;
    private long fileCount;

    public long getUsedSpace() {
        return usedSpace;
    }

    public void setUsedSpace(long usedSpace) {
        this.usedSpace = usedSpace;
    }

    public long getFileCount() {
        return fileCount;
    }

    public void setFileCount(long fileCount) {
        this.fileCount = fileCount;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
