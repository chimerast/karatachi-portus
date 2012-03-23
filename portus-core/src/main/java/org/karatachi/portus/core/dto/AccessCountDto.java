package org.karatachi.portus.core.dto;

import java.io.Serializable;
import java.util.Date;

public class AccessCountDto implements Serializable {
    private static final long serialVersionUID = 1L;

    public Date date;
    public long count;
}
