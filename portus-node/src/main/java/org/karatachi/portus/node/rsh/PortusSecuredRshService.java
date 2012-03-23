package org.karatachi.portus.node.rsh;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.karatachi.classloader.PackageDir;
import org.karatachi.net.rsh.RshService;
import org.karatachi.net.shell.Command;
import org.karatachi.portus.node.AssemblyInfo;
import org.karatachi.portus.node.PortusSSLProtocolSocketFactory;

public class PortusSecuredRshService extends RshService {
    public PortusSecuredRshService(int port) throws IOException {
        super(PortusSSLProtocolSocketFactory.getInstance().createServerSocket(
                port, true), getCommands(), AssemblyInfo.DEFAULT_CHARSET);
    }

    public static Map<String, Class<? extends Command>> getCommands() {
        Map<String, Class<? extends Command>> ret =
                new HashMap<String, Class<? extends Command>>();
        PackageDir dir =
                new PackageDir(PortusSecuredRshService.class.getClassLoader(),
                        "org.karatachi.portus.node.command");
        for (Class<? extends Command> clazz : dir.getClasses(Command.class)) {
            try {
                Command command = clazz.newInstance();
                ret.put(command.getCommand(), clazz);
            } catch (IllegalAccessException ignore) {
            } catch (InstantiationException ignore) {
            }
        }
        return ret;
    }
}
