package org.karatachi.portus.core.dao;

import java.util.Date;

import org.seasar.dao.annotation.tiger.Sql;

public interface NodeCountDao {
    @Sql("INSERT INTO portus.node_count(date, node_id, count, transfer) VALUES(?, ?, ?, ?)")
    public int insert(Date date, long nodeId, long count, long transfer);

    @Sql("DELETE FROM portus.node_count WHERE date=? AND node_id=?")
    public int delete(Date date, long nodeId);
}
