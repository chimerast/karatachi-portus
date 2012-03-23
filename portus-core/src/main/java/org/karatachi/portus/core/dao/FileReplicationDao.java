package org.karatachi.portus.core.dao;

import java.util.List;

import org.karatachi.portus.core.entity.FileReplication;
import org.seasar.dao.annotation.tiger.Arguments;
import org.seasar.dao.annotation.tiger.Query;
import org.seasar.dao.annotation.tiger.S2Dao;
import org.seasar.dao.annotation.tiger.Sql;

@S2Dao(bean = FileReplication.class)
public interface FileReplicationDao {
    @Arguments("id")
    public FileReplication select(long id);

    @Sql("SELECT max(id) FROM portus.file_replication")
    public long selectMaxId();

    @Arguments("ids")
    @Query("id IN /*$ids*/")
    public List<FileReplication> getFileStatusList(String ids);

    @Query("available >= 1 AND available < regulated")
    public List<FileReplication> selectForReplication();

    @Arguments("minimam_replication")
    @Query("available < /*minimam_replication*/")
    public List<FileReplication> selectForDirectReplication(
            int minimamReplication);

    @Sql("UPDATE file_info SET replicated=/*replicated*/, available=/*available*/ WHERE id=/*id*/")
    @Arguments({ "id", "replicated", "available" })
    public int updateFileInfo(long fileId, int replicated, int available);

    @Sql("UPDATE file_replication SET available=available+1 WHERE id IN (SELECT file_id FROM storedinfo WHERE node_id=?)")
    public int incrementByNode(long nodeId);

    @Sql("UPDATE file_replication SET available=available-1 WHERE id IN (SELECT file_id FROM storedinfo WHERE node_id=?)")
    public int decrementByNode(long nodeId);
}
