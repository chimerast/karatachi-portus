package org.karatachi.portus.core.dto;

import java.io.Serializable;

import org.karatachi.portus.core.entity.File;
import org.seasar.framework.container.annotation.tiger.Component;
import org.seasar.framework.container.annotation.tiger.InstanceType;


@Component(instance = InstanceType.SESSION)
public class FileAccessDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private File dir;

    public File getDir() {
        return dir;
    }

    public void setDir(File dir) {
        this.dir = dir;
    }
}
