package org.karatachi.portus.core.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.karatachi.portus.core.dto.AccessCountDto;
import org.seasar.dao.annotation.tiger.Arguments;
import org.seasar.dao.annotation.tiger.Sql;

public interface AccessCountDao {
    @Sql("INSERT INTO portus.access_count(date, code, method, full_path, domain_id, count, transfer) VALUES(?, ?, ?, ?, ?, ?, ?)")
    public int insert(Date date, int code, String method, String fullPath,
            long domainId, long count, long transfer);

    @Sql("DELETE FROM portus.access_count WHERE date=? AND code=? AND method=? AND full_path=?")
    public int delete(Date date, int code, String method, String fullPath);

    @Sql("SELECT count FROM portus.access_count WHERE date=? AND code=200 AND method='GET' AND full_path=?")
    public long selectAccessCount(Date date, String fullPath);

    @Sql("SELECT date, count FROM portus.access_count WHERE date>=date_trunc('day', /*date*/ ::timestamp) AND date<date_trunc('day', /*date*/ ::timestamp) + '1 day'::interval AND code=200 AND method='GET' AND full_path=/*full_path*/")
    @Arguments({ "date", "full_path" })
    public List<AccessCountDto> selectAccessCountInDay(Date date,
            String fullPath);

    @Sql("SELECT date_trunc('day', /*date*/ ::timestamp) AS from, date_trunc('day', /*date*/ ::timestamp)+'1 month'::interval-'1 day'::interval AS to, coalesce(sum(count), 0) AS totalAccess, coalesce(sum(transfer), 0) AS totalTransfer FROM portus.access_count "
            + "WHERE date>=date_trunc('day', /*date*/ ::timestamp) AND date<date_trunc('day', /*date*/ ::timestamp)+'1 month'::interval AND code=200 AND method='GET' AND domain_id=/*domain_id*/")
    @Arguments({ "domain_id", "date" })
    public Map<String, Object> selectAccessCountByDomain(long domainId,
            Date date);
}
