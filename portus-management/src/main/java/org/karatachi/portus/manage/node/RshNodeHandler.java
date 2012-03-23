package org.karatachi.portus.manage.node;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.karatachi.net.rsh.RshHandler;
import org.karatachi.net.shell.CommandResponse;
import org.karatachi.net.shell.CommandResponseCallback;
import org.karatachi.portus.core.entity.Node;
import org.karatachi.portus.manage.AssemblyInfo;

public class RshNodeHandler extends NodeHandler {
    private final RshHandler handler;

    public RshNodeHandler(Node chassis) throws IOException {
        super(chassis);

        RshHandler handler = null;
        try {
            InetSocketAddress sockaddr =
                    new InetSocketAddress(chassis.ipAddress, chassis.ctrlPort);
            handler = new RshHandler(sockaddr, AssemblyInfo.DEFAULT_CHARSET);
        } catch (RuntimeException e) {
            if (handler != null) {
                handler.close();
            }
            throw e;
        }
        this.handler = handler;
    }

    @Override
    public CommandResponse executeCommand(String command,
            CommandResponseCallback callback) throws IOException {
        return handler.executeCommand(command, callback);
    }

    @Override
    public void close() throws IOException {
        handler.close();
    }
}
