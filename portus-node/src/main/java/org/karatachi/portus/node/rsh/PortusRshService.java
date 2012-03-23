package org.karatachi.portus.node.rsh;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

import org.karatachi.classloader.PackageDir;
import org.karatachi.net.rsh.RshService;
import org.karatachi.net.shell.Command;
import org.karatachi.portus.node.AssemblyInfo;
import org.karatachi.portus.node.Service;

public class PortusRshService extends RshService {
    public PortusRshService(int port) throws IOException {
        super(new ServerSocket(port), getCommands(),
                AssemblyInfo.DEFAULT_CHARSET);
    }

    @Override
    protected boolean checkRemoteAddr(InetSocketAddress addr) {
        return Service.debug
                || addr.getAddress().getHostAddress().startsWith("127.0.0.1")
                || addr.getAddress().getHostAddress().startsWith(
                        "192.168.100.151")
                || addr.getAddress().getHostAddress().startsWith(
                        AssemblyInfo.CONTROL_ALLOWED_ADDRESS);
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
