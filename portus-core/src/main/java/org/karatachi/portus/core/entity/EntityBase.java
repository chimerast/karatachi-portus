package org.karatachi.portus.core.entity;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;

public class EntityBase implements Serializable {
    private static final long serialVersionUID = 1L;

    public Date registered;
    public Date updated;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
