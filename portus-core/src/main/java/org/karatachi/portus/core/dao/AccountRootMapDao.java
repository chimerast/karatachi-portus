package org.karatachi.portus.core.dao;

import java.util.List;

import org.karatachi.portus.core.entity.AccountRootMap;
import org.seasar.dao.annotation.tiger.NoPersistentProperty;
import org.seasar.dao.annotation.tiger.Query;
import org.seasar.dao.annotation.tiger.S2Dao;

@S2Dao(bean = AccountRootMap.class)
public interface AccountRootMapDao {
    @NoPersistentProperty("registered")
    int insert(AccountRootMap map);

    @NoPersistentProperty("registered")
    int update(AccountRootMap map);

    int delete(AccountRootMap map);

    @Query("account_id=?")
    List<AccountRootMap> selectInAccount(long accountId);

    @Query("file_id=?")
    List<AccountRootMap> selectInRoot(long fileId);

    @Query("account_id=? AND file_id=?")
    int deleteByAccount(long accountId, long fileId);
}
