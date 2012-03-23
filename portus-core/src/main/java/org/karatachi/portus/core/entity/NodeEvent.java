package org.karatachi.portus.core.entity;

import java.util.Date;

import org.seasar.dao.annotation.tiger.Bean;
import org.seasar.dao.annotation.tiger.Id;
import org.seasar.dao.annotation.tiger.IdType;

@Bean(table = "portus.node_event", timeStampProperty = "updated")
public class NodeEvent extends EntityBase {
    private static final long serialVersionUID = 1L;

    public static final String BOOTSTRAP_UPDATE = "bootstrap_update";
    public static final String NODE_UPDATE = "node_update";
    public static final String NODE_CHECK = "node_check";
    public static final String NODE_MAINTENANCE = "node_maintenance";

    @Id(value = IdType.SEQUENCE, sequenceName = "node_event_id_seq")
    public long id;

    public long nodeId;

    public String name;
    public Date date;
}
