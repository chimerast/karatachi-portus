package org.karatachi.portus.manage.dto;

import java.io.Serializable;

public class RegulatedReplicationDto implements Serializable {
    private static final long serialVersionUID = 1L;

    public long id;
    public double hitsPerSec;
    public double sentPerSec;
    public int regulatedReplication;
    public int necessaryReplication;
}
