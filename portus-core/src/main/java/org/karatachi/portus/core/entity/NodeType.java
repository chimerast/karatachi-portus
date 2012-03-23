package org.karatachi.portus.core.entity;

import org.seasar.dao.annotation.tiger.Bean;
import org.seasar.dao.annotation.tiger.Id;
import org.seasar.dao.annotation.tiger.IdType;

@Bean(table = "portus.node_type", timeStampProperty = "updated")
public class NodeType extends EntityBase {
    private static final long serialVersionUID = 1L;

    @Id(value = IdType.SEQUENCE, sequenceName = "node_type_seq")
    public long id;

    public String name;
}
