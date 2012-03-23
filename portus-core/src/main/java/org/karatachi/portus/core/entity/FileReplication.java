package org.karatachi.portus.core.entity;

import java.io.Serializable;

import org.seasar.dao.annotation.tiger.Bean;
import org.seasar.dao.annotation.tiger.Id;

@Bean(table = "portus.file_replication")
public class FileReplication implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    public long id;

    public int regulated;
    public int available;
}
