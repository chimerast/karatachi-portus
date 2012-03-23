package org.karatachi.portus.core.entity;

import org.seasar.dao.annotation.tiger.Bean;
import org.seasar.dao.annotation.tiger.Id;
import org.seasar.dao.annotation.tiger.IdType;

@Bean(table = "portus.domain", timeStampProperty = "updated")
public class Domain extends EntityBase {
    private static final long serialVersionUID = 1L;

    @Id(value = IdType.SEQUENCE, sequenceName = "domain_id_seq")
    public long id;

    public long customerId;

    public String name;
    public boolean valid;
    public long quota;
}
