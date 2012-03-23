package org.karatachi.portus.core.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.karatachi.portus.core.entity.File;
import org.seasar.dao.annotation.tiger.Arguments;
import org.seasar.dao.annotation.tiger.NoPersistentProperty;
import org.seasar.dao.annotation.tiger.Query;
import org.seasar.dao.annotation.tiger.S2Dao;
import org.seasar.dao.annotation.tiger.Sql;

@S2Dao(bean = File.class)
public interface FileDao {
    @NoPersistentProperty({ "registered", "fullPath", "actualPublished",
            "actualAuthorized", "actualOpenDate", "actualCloseDate" })
    public int insert(File file);

    @NoPersistentProperty({ "registered", "fullPath", "actualPublished",
            "actualAuthorized", "actualOpenDate", "actualCloseDate" })
    public int update(File file);

    public int delete(File file);

    @Arguments("id")
    public File select(long id);

    @Arguments("full_path")
    public File selectByFullPath(String fullPath);

    @Query("id=/*id*/ FOR UPDATE")
    @Arguments("id")
    public File selectForUpdate(long id);

    @Query("parent_id=/*parent_id*/ ORDER BY directory DESC, name")
    @Arguments("parent_id")
    public List<File> selectFilesInDirectory(long parentId);

    @Query("parent_id=/*parent_id*/ AND name=/*name*/")
    @Arguments({ "parent_id", "name" })
    public File selectFileInDirectory(long parentId, String name);

    @Query("parent_id=0 AND domain_id=/*domain_id*/")
    @Arguments({ "domain_id" })
    public File selectDomainDirectory(long domainId);

    @Sql("SELECT id FROM portus.file WHERE NOT directory AND digest IS NULL")
    public List<Long> selectForCalculateDigest();

    @Sql("UPDATE portus.file SET size=/*size*/, digest=/*digest*/ WHERE id=/*id*/")
    @Arguments({ "id", "size", "digest" })
    public int updateDigest(long id, long size, String digest);

    @Sql("SELECT count(*) AS totalCount, coalesce(sum(actual_size), 0) AS totalSize FROM portus.file WHERE domain_id=/*domain_id*/ AND NOT directory")
    @Arguments({ "domain_id" })
    public Map<String, Number> selectDomainInfo(long domainId);

    @Query("parent_id=/*parent_id*/ AND name LIKE /*name*/ || '%'"
            + "/*IF registered_before != null*/ AND registered <  /*registered_before*//*END*/"
            + "/*IF registered_after  != null*/ AND registered >= /*registered_after*/ /*END*/")
    @Arguments({ "parent_id", "name", "registered_before", "registered_after" })
    public List<File> search(long parentId, String name, Date registeredBefore,
            Date registeredAfter);

    @Sql("SELECT max(id) FROM file")
    public long selectMaxId();
}
