package org.karatachi.portus.core.entity;

import java.io.Serializable;

public class LocalFile implements Serializable, Comparable<LocalFile> {
    private static final long serialVersionUID = 1L;

    public java.io.File file;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LocalFile) {
            return file.equals(((LocalFile) obj).file);
        } else {
            return false;
        }
    }

    @Override
    public int compareTo(LocalFile o) {
        return file.compareTo(o.file);
    }
}
