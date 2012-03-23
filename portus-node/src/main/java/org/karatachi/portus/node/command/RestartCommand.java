package org.karatachi.portus.node.command;

import java.io.IOException;

import org.karatachi.net.shell.Command;
import org.karatachi.portus.node.AbstractCommand;
import org.karatachi.portus.node.AssemblyInfo;
import org.karatachi.portus.node.Service;

public class RestartCommand extends AbstractCommand {
    @Override
    protected int command(String[] args) throws IOException {
        if (args.length != 1) {
            return Command.INVALID_ARGUMENT_COUNTS;
        }

        try {
            Service.stop();
            Service.join(3000);
            Runtime.getRuntime().exec(
                    String.format(AssemblyInfo.JAVA_COMMAND,
                            AssemblyInfo.PATH_BOOTSTRAP));
            sendStatus(200, "OK");
            System.exit(0);
        } catch (Exception e) {
            sendStatus(400, "Could Not Execute Process");
        }

        return Command.OK;
    }

    @Override
    public String getCommand() {
        return "restart";
    }
}
