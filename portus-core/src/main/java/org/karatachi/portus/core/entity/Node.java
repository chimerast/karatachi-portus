package org.karatachi.portus.core.entity;

import org.seasar.dao.annotation.tiger.Bean;
import org.seasar.dao.annotation.tiger.Id;

@Bean(table = "portus.node", timeStampProperty = "updated")
public class Node extends EntityBase {
    private static final long serialVersionUID = 1L;

    @Id
    public long id;

    public long nodeBlockId;
    public long nodeTypeId;

    public int status;
    public int update;

    public String ipAddress;
    public int ctrlPort;
    public int httpPort;

    public int bootstrapRevision;
    public int nodeRevision;
    public int protocolRevision;

    public static final long DATACENTER_ID = -1L;

    public static final long TYPE_NORMAL = 0;
    public static final long TYPE_FLASH_MEDIA_SERVER = 1;

    public static final int STATUS_REMOVED = -1;
    public static final int STATUS_DOWN = 0;
    public static final int STATUS_OK = 1;
    public static final int STATUS_OVERLOADED = 2;
    public static final int STATUS_NOT_WORKING = 3;
    public static final int STATUS_MAINTENANCE = 4;

    @Override
    public String toString() {
        return String.format("node[id=%012X, status=%d, ip_address=%s]", id,
                status, ipAddress);
    }
}
