package org.karatachi.portus.bootstrap;

import java.io.File;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.karatachi.system.ClassPropertyLoader;
import org.karatachi.system.SystemInfo;

public class AssemblyInfo {
    public static String URL_REGISTER;
    public static String URL_UPDATE;

    public static String PATH_BOOTSTRAP = "/portus/bootstrap";
    public static String PATH_NODE = "/portus/node";

    public static long NODE_ID;

    public static int BOOTSTRAP_REVISION;
    public static String BOOTSTRAP_VERSION;

    static {
        NODE_ID = SystemInfo.getMacAddress("eth1");
        if (NODE_ID == 0) {
            NODE_ID = SystemInfo.getMacAddress("eth0");
        }

        ClassPropertyLoader loader = new ClassPropertyLoader();
        loader.loadIfNotContains(new File("/portus/portus.properties"));
        loader.loadIfNotContains(AssemblyInfo.class.getResourceAsStream("/portus-bootstrap.properties"));
        loader.setClassProperties(AssemblyInfo.class);

        HttpsURLConnection.setDefaultSSLSocketFactory(PortusSSLSocketFactory.getInstance().getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
    }
}
