package org.karatachi.portus.core.entity;

import org.karatachi.portus.core.type.AccountRole;
import org.seasar.dao.annotation.tiger.Bean;
import org.seasar.dao.annotation.tiger.Id;
import org.seasar.dao.annotation.tiger.IdType;

@Bean(table = "portus.account", timeStampProperty = "updated")
public class Account extends EntityBase {
    private static final long serialVersionUID = 1L;

    @Id(value = IdType.SEQUENCE, sequenceName = "account_id_seq")
    public long id;

    public long customerId;

    public String name;
    public boolean valid;
    public long quota;
    public AccountRole role;
    public String password;

    public String homedir;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Account) {
            return id == ((Account) obj).id;
        } else {
            return false;
        }
    }
}
