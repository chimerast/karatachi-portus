package org.karatachi.portus.core;

import java.io.File;

import org.karatachi.system.ClassPropertyLoader;

public class AssemblyInfo {
    public static String SERVICE_NAME;
    public static String ROOT_SERVER;
    public static String DATA_SERVER;

    public static String PATH_RAW_DATA = "/portus/upload";
    public static String PATH_REG_DATA = "/portus/registered";
    public static String PATH_ACCESS_LOG = "/portus/accesslog";

    public static String PATH_NODE_STABLE = "/portus/system/stable";
    public static String PATH_NODE_TESTING = "/portus/system/testing";
    public static String PATH_NODE_UNSTABLE = "/portus/system/unstable";
    public static String PATH_BOOTSTRAP = "/portus/system/bootstrap";

    public static String PATH_LOG = "/portus/log";

    public static int REPLICATION_DEFAULT = 5;
    public static int REPLICATION_THRESHOLD = 5;
    public static int REPLICATION_MINIMAM = 3;

    public static int OVERLOAD_THRESHOLD = 50;

    static {
        ClassPropertyLoader loader = new ClassPropertyLoader();
        loader.loadIfNotContains(new File("/portus/portus.properties"));
        loader.loadIfNotContains(AssemblyInfo.class, "/portus-core.properties");
        loader.setClassProperties(AssemblyInfo.class);
    }
}
