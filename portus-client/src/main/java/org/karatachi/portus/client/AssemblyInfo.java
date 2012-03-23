package org.karatachi.portus.client;

import java.io.File;

import org.karatachi.system.ClassPropertyLoader;

public class AssemblyInfo {
    public static String SERVICE_NAME;

    public static String REST_API_URL;
    public static String FTP_HOSTNAME;

    public static String TARGET_DOMAIN;

    public static String TARGET_USERID = "artificer";
    public static String TARGET_PASSWORD = "artificer";

    static {
        ClassPropertyLoader loader = new ClassPropertyLoader();
        loader.loadIfNotContains(new File("/portus/portus.properties"));
        loader.loadIfNotContains(AssemblyInfo.class.getResourceAsStream("/portus-client.properties"));
        loader.setClassProperties(AssemblyInfo.class);

        String keyStorePath =
                String.format("/%s!%s.keystore", AssemblyInfo.SERVICE_NAME,
                        AssemblyInfo.TARGET_USERID);
        PortusSSLSocketFactory.initialize(keyStorePath, "/truststore");
    }
}
