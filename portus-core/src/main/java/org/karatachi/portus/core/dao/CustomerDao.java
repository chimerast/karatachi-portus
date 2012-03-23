package org.karatachi.portus.core.dao;

import java.util.List;

import org.karatachi.portus.core.entity.Customer;
import org.seasar.dao.annotation.tiger.Arguments;
import org.seasar.dao.annotation.tiger.NoPersistentProperty;
import org.seasar.dao.annotation.tiger.Query;
import org.seasar.dao.annotation.tiger.S2Dao;

@S2Dao(bean = Customer.class)
public interface CustomerDao {
    @NoPersistentProperty("registered")
    int insert(Customer customer);

    @NoPersistentProperty("registered")
    int update(Customer customer);

    int delete(Customer customer);

    @Arguments("id")
    Customer select(long id);

    @Query("id=/*id*/ FOR UPDATE")
    @Arguments("id")
    Customer selectForUpdate(long id);

    @Query("id<>0 ORDER BY CASE WHEN parent_id=0 THEN id ELSE parent_id END, parent_id, id")
    List<Customer> selectAll();

    @Query("parent_id=? AND id<>0 ORDER BY id")
    List<Customer> selectInCustomer(long parentId);
}
