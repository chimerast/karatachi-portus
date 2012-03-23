package org.karatachi.portus.core.dao;

import java.util.List;

import org.karatachi.portus.core.entity.Node;
import org.seasar.dao.annotation.tiger.Arguments;
import org.seasar.dao.annotation.tiger.Query;
import org.seasar.dao.annotation.tiger.S2Dao;
import org.seasar.dao.annotation.tiger.Sql;
import org.seasar.dao.annotation.tiger.SqlFile;

@S2Dao(bean = Node.class)
public interface NodeDao {
    public List<Node> selectAll();

    @Query("status>=0")
    public List<Node> selectActive();

    @Query("ORDER BY (SELECT date FROM portus.node_event WHERE portus.node_event.node_id=portus.node.id AND portus.node_event.name=?)")
    public List<Node> selectByEvent(String name);

    @Sql("UPDATE node SET status=/*status*/ WHERE id=/*node_id*/ AND status IN (0, 1, 2)")
    @Arguments({ "node_id", "status" })
    public int updateStatus(long nodeId, int status);

    @Sql("UPDATE node SET status=/*status*/ WHERE id=/*node_id*/")
    @Arguments({ "node_id", "status" })
    public int updateStatusForce(long nodeId, int status);

    @SqlFile
    @Arguments({ "file_id" })
    public Node selectReplicationDestination(long fileId);

    @SqlFile
    @Arguments({ "file_id", "node_type_id" })
    public Node selectReplicationDestinationWithType(long fileId,
            long nodeTypeId);

    @SqlFile
    @Arguments({ "file_id" })
    public Node selectReplicationSource(long fileId);

    @Sql("DELETE FROM storedinfo WHERE status <= 0 AND from_node_id <> -1 AND hostname = /*hostname*/")
    @Arguments({ "hostname" })
    public int deleteUncommitedReplication(String hostname);

    @Sql("DELETE FROM storedinfo WHERE status <= 0 AND from_node_id = -1 AND hostname = /*hostname*/")
    @Arguments({ "hostname" })
    public int deleteUncommitedDirectReplication(String hostname);
}
