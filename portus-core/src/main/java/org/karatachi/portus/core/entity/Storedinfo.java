package org.karatachi.portus.core.entity;

import org.seasar.dao.annotation.tiger.Bean;
import org.seasar.dao.annotation.tiger.Id;
import org.seasar.dao.annotation.tiger.IdType;
import org.seasar.dao.annotation.tiger.Relation;

@Bean(table = "portus.storedinfo", timeStampProperty = "updated")
public class Storedinfo extends EntityBase {
    private static final long serialVersionUID = 1L;

    @Id(value = IdType.SEQUENCE, sequenceName = "storedinfo_id_seq")
    public long id;

    public long fileId;
    public long nodeId;
    public long fromNodeId;

    public String digest;
    public String hostname;

    @Relation(relationNo = 0, relationKey = "file_id:id")
    public File file;
}
