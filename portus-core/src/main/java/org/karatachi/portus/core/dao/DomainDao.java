package org.karatachi.portus.core.dao;

import java.util.List;

import org.karatachi.portus.core.entity.Domain;
import org.seasar.dao.annotation.tiger.Arguments;
import org.seasar.dao.annotation.tiger.NoPersistentProperty;
import org.seasar.dao.annotation.tiger.Query;
import org.seasar.dao.annotation.tiger.S2Dao;

@S2Dao(bean = Domain.class)
public interface DomainDao {
    @NoPersistentProperty("registered")
    int insert(Domain domain);

    @NoPersistentProperty("registered")
    int update(Domain domain);

    int delete(Domain domain);

    @Arguments("id")
    Domain select(long id);

    @Query("id=/*id*/ FOR UPDATE")
    @Arguments("id")
    Domain selectForUpdate(long id);

    @Query("ORDER BY customer_id, name")
    List<Domain> selectAll();

    @Query("customer_id=? ORDER BY name")
    List<Domain> selectInCustomer(long customerId);
}
