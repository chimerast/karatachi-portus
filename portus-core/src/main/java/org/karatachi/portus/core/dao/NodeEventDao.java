package org.karatachi.portus.core.dao;

import org.karatachi.portus.core.entity.NodeEvent;
import org.seasar.dao.annotation.tiger.Arguments;
import org.seasar.dao.annotation.tiger.NoPersistentProperty;
import org.seasar.dao.annotation.tiger.Query;
import org.seasar.dao.annotation.tiger.S2Dao;

@S2Dao(bean = NodeEvent.class)
public interface NodeEventDao {
    @NoPersistentProperty("registered")
    public int insert(NodeEvent nodeEvent);

    @NoPersistentProperty("registered")
    public int update(NodeEvent nodeEvent);

    public int delete(NodeEvent nodeEvent);

    @Arguments("id")
    public NodeEvent select(long id);

    @Query("node_id=/*node_id*/ AND name=/*name*/")
    @Arguments( { "node_id", "name" })
    public NodeEvent selectByNode(long nodeId, String name);
}
