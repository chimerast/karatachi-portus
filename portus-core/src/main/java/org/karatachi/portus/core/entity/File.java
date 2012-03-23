package org.karatachi.portus.core.entity;

import java.util.Date;

import org.seasar.dao.annotation.tiger.Bean;
import org.seasar.dao.annotation.tiger.Id;
import org.seasar.dao.annotation.tiger.IdType;

@Bean(table = "portus.file", timeStampProperty = "updated")
public class File extends EntityBase implements Comparable<File> {
    private static final long serialVersionUID = 1L;

    @Id(value = IdType.SEQUENCE, sequenceName = "file_id_seq")
    public long id;

    public long parentId;
    public long domainId;

    public boolean directory;
    public String name;
    public long size;
    public String digest;

    public boolean published;
    public boolean authorized;
    public Date openDate;
    public Date closeDate;
    public int replication;
    public String referer;

    public String fullPath;
    public boolean actualPublished;
    public boolean actualAuthorized;
    public Date actualOpenDate;
    public Date actualCloseDate;
    public long actualSize;

    public long fileTypeId;

    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_FLASH_STREAMING = 1;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof File) {
            return fullPath.equals(((File) obj).fullPath);
        } else {
            return false;
        }
    }

    @Override
    public int compareTo(File o) {
        return fullPath.compareTo(o.fullPath);
    }
}
