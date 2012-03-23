package org.karatachi.portus.api.dao;

import org.seasar.dao.annotation.tiger.Sql;

public interface SSLCrlDao {
    @Sql("SELECT count(*) FROM ssl_crl WHERE serial=?")
    public int containsSerial(long serial);
}
