package org.karatachi.portus.core.entity;

import org.seasar.dao.annotation.tiger.Bean;
import org.seasar.dao.annotation.tiger.Id;
import org.seasar.dao.annotation.tiger.IdType;

@Bean(table = "portus.account_root_map", timeStampProperty = "updated")
public class AccountRootMap extends EntityBase {
    private static final long serialVersionUID = 1L;

    @Id(value = IdType.SEQUENCE, sequenceName = "account_root_map_id_seq")
    public long id;

    public long accountId;
    public long fileId;
}
