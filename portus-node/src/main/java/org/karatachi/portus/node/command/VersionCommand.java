package org.karatachi.portus.node.command;

import java.io.IOException;

import org.karatachi.net.shell.Command;
import org.karatachi.portus.node.AbstractCommand;
import org.karatachi.portus.node.AssemblyInfo;

public class VersionCommand extends AbstractCommand {
    @Override
    protected int command(String[] args) throws IOException {
        sendStatus(200, "OK");
        sendHeader("Server", AssemblyInfo.getServiceVersion());
        sendHeader("Protocol-Revision", AssemblyInfo.PROTOCOL_REVISION);
        sendHeader("Bootstrap-Revision", AssemblyInfo.BOOTSTRAP_REVISION);
        sendHeader("Node-Revision", AssemblyInfo.NODE_REVISION);
        return Command.OK;
    }

    @Override
    public String getCommand() {
        return "version";
    }
}
