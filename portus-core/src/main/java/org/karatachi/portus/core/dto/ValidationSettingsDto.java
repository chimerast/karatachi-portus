package org.karatachi.portus.core.dto;

import java.io.Serializable;

import org.seasar.framework.container.annotation.tiger.Component;
import org.seasar.framework.container.annotation.tiger.InstanceType;

@Component(instance = InstanceType.REQUEST)
public class ValidationSettingsDto implements Serializable {
    private static final long serialVersionUID = 1L;

    public boolean relaxFilenameValidation;
}
