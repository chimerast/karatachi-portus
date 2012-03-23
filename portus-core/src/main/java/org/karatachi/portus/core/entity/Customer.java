package org.karatachi.portus.core.entity;

import org.karatachi.portus.core.type.CustomerRole;
import org.seasar.dao.annotation.tiger.Bean;
import org.seasar.dao.annotation.tiger.Id;
import org.seasar.dao.annotation.tiger.IdType;

@Bean(table = "portus.customer", timeStampProperty = "updated")
public class Customer extends EntityBase {
    private static final long serialVersionUID = 1L;

    @Id(value = IdType.SEQUENCE, sequenceName = "customer_id_seq")
    public long id;

    public long parentId;

    public String name;
    public boolean valid;
    public long quota;
    public CustomerRole role;
}
