package org.karatachi.portus.core.dao;

import java.util.List;

import org.karatachi.portus.core.entity.Account;
import org.seasar.dao.annotation.tiger.Arguments;
import org.seasar.dao.annotation.tiger.NoPersistentProperty;
import org.seasar.dao.annotation.tiger.Query;
import org.seasar.dao.annotation.tiger.S2Dao;

@S2Dao(bean = Account.class)
public interface AccountDao {
    @NoPersistentProperty("registered")
    int insert(Account account);

    @NoPersistentProperty("registered")
    int update(Account account);

    int delete(Account account);

    @Arguments("id")
    Account select(long id);

    @Query("id=/*id*/ FOR UPDATE")
    @Arguments("id")
    Account selectForUpdate(long id);

    @Query("ORDER BY customer_id, name")
    List<Account> selectAll();

    @Arguments("name")
    Account selectByName(String name);

    @Query("customer_id=? ORDER BY name")
    List<Account> selectInCustomer(long customerId);
}
