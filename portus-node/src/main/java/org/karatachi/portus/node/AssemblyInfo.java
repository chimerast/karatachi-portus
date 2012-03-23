package org.karatachi.portus.node;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.jar.JarFile;

import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.karatachi.portus.node.http.PortusHttpService;
import org.karatachi.system.ClassPropertyLoader;
import org.karatachi.system.SystemInfo;

public class AssemblyInfo {
    public static String URL_REGISTER;
    public static String URL_UPDATE;

    public static String JAVA_COMMAND;

    public static String PATH_BOOTSTRAP;
    public static String PATH_RAW_DATA;
    public static String PATH_LOG;

    public static long NODE_ID;

    public static int BOOTSTRAP_REVISION;
    public static int NODE_REVISION;
    public static String NODE_VERSION;

    public static int PROTOCOL_REVISION;
    public static int REQUIRED_BOOTSTRAP_REVESION;

    public static int HTTP_PORT;
    public static int RSH_PORT;
    public static int SRSH_PORT;

    public static String CONTROL_ALLOWED_ADDRESS;

    public static final String ROUTER_ADMIN_ID = "admin";
    public static final String ROUTER_ADMIN_PASS = "8zLyV7Uf";

    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    public static void loadBootstrapRevision() {
        try {
            JarFile jar = new JarFile(PATH_BOOTSTRAP);
            try {
                Properties props = new Properties();
                props.load(jar.getInputStream(jar.getJarEntry("portus-bootstrap.properties")));
                BOOTSTRAP_REVISION =
                        Integer.parseInt(props.getProperty("AssemblyInfo.BOOTSTRAP_REVISION"));
            } finally {
                jar.close();
            }
        } catch (Exception ignore) {
        }
    }

    static {
        NODE_ID = SystemInfo.getMacAddress("eth1");
        if (NODE_ID == 0) {
            NODE_ID = SystemInfo.getMacAddress("eth0");
        }

        ClassPropertyLoader loader = new ClassPropertyLoader();
        loader.loadIfNotContains(new File("/portus/portus.properties"));
        loader.loadIfNotContains(AssemblyInfo.class, "/portus-node.properties");
        loader.setClassProperties(AssemblyInfo.class);
        loader.setClassProperties(PortusHttpService.class);

        loadBootstrapRevision();

        ProtocolSocketFactory sslfactory =
                PortusSSLProtocolSocketFactory.getInstance();
        Protocol.registerProtocol("https", new Protocol("https", sslfactory,
                443));
    }

    public static String getServiceVersion() {
        return String.format("PORTUS/%s Karatachi Portus %s (%s)",
                NODE_VERSION, NODE_VERSION, VERSION_NAME[VERSION_NAME.length
                        - PROTOCOL_REVISION]);
    }

    public static final String[] VERSION_NAME = { "Alice", "Alice's Sister",
            "The White Rabbit", "Alice's Cat, Dinah", "The Mouse", "The Duck",
            "The Dodo", "The Lory", "The Eaglet",
            "An old Crab and her daughter", "An old Magpie", "A Canary", "Pat",
            "Bill the Lizard", "The Puppy", "The Caterpillar", "The Pigeon",
            "The Fish-Footman", "The Frog-Footman", "The Duchess", "The Baby",
            "The Cook", "The Cheshire Cat", "The Hatter", "The March Hare",
            "The Dormouse", "The Queen of Hearts", "Two, Five & Seven (cards)",
            "The Knave of Hearts", "The King of Hearts", "The Executioner",
            "The Gryphon", "The Mock Turtle", "The Jurors" };
}
