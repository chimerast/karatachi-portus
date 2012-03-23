package org.karatachi.portus.manage;

import java.io.File;
import java.nio.charset.Charset;

import org.karatachi.system.ClassPropertyLoader;

public class AssemblyInfo extends org.karatachi.portus.core.AssemblyInfo {
    public static final int BOOTSTRAP = -1;
    public static final int STABLE = 0;
    public static final int TESTING = 1;
    public static final int UNSTABLE = 2;

    public static final int REPLICATION_POWER = 2;

    public static int CONNECTION_TIMEOUT = 2000;
    public static int READ_TIMEOUT = 30000;

    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    static {
        ClassPropertyLoader loader = new ClassPropertyLoader();
        loader.loadIfNotContains(new File("/portus/portus.properties"));
        loader.loadIfNotContains(AssemblyInfo.class,
                "/portus-management.properties");
        loader.setClassProperties(AssemblyInfo.class);
    }
}
