package org.karatachi.portus.core.dao;

import java.util.List;

import org.karatachi.portus.core.entity.Storedinfo;
import org.seasar.dao.annotation.tiger.Arguments;
import org.seasar.dao.annotation.tiger.NoPersistentProperty;
import org.seasar.dao.annotation.tiger.Query;
import org.seasar.dao.annotation.tiger.S2Dao;
import org.seasar.dao.annotation.tiger.Sql;

@S2Dao(bean = Storedinfo.class)
public interface StoredinfoDao {
    @NoPersistentProperty("registered")
    public int insert(Storedinfo storedinfo);

    @NoPersistentProperty("registered")
    public int update(Storedinfo storedinfo);

    public int delete(Storedinfo storedinfo);

    @Query("node_id=/*node_id*/")
    @Arguments("node_id")
    public List<Storedinfo> selectInNode(long nodeId);

    @Query("node_id=/*node_id*/ AND file.name like /*prefix*/ || '%'")
    @Arguments( { "node_id", "prefix" })
    public List<Storedinfo> selectInNodeWithPrefix(long nodeId, String prefix);

    @Query("file_id=/*file_id*/")
    @Arguments("file_id")
    public List<Storedinfo> selectByFile(long fileId);

    @Sql("DELETE FROM storedinfo WHERE file_id=/*file_id*/ AND node_id=/*node_id*/")
    @Arguments( { "node_id", "file_id" })
    public int deleteFileInNode(long nodeId, long fileId);

    @Sql("DELETE FROM storedinfo WHERE file_id=/*file_id*/")
    @Arguments("file_id")
    public int deleteByFile(long fileId);
}
