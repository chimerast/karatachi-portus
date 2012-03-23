package org.karatachi.portus.core.dao;

import org.seasar.dao.annotation.tiger.Arguments;
import org.seasar.dao.annotation.tiger.Sql;

public interface ConfigDao {
    @Sql("SELECT value FROM portus.config WHERE key=?")
    public String select(String key);

    @Sql("INSERT INTO portus.config VALUES(/*key*/, /*value*/)")
    @Arguments( { "key", "value" })
    public int insert(String key, String value);

    @Sql("UPDATE portus.config SET value=/*value*/ WHERE key=/*key*/")
    @Arguments( { "key", "value" })
    public int update(String key, String value);

    @Sql("DELETE portus.config WHERE key=?")
    public int delete(String key);
}
