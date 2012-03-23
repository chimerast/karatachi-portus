package org.karatachi.portus.node.command;

import java.io.File;
import java.io.IOException;

import org.karatachi.net.shell.Command;
import org.karatachi.portus.node.AbstractCommand;
import org.karatachi.portus.node.AssemblyInfo;

public class DeleteCommand extends AbstractCommand {
    @Override
    protected int command(String[] args) throws IOException {
        if (args.length != 2) {
            return Command.INVALID_ARGUMENT_COUNTS;
        }

        StringBuilder name = new StringBuilder(args[1]);
        name.insert(6, '/');
        name.insert(4, '/');
        name.insert(2, '/');

        if (name.indexOf(".") != -1) {
            sendStatus(400, "Invalid Path");
            return Command.OK;
        }

        File file = new File(AssemblyInfo.PATH_RAW_DATA, name.toString());
        if (file.exists()) {
            if (!file.isFile()) {
                sendStatus(400, "Is Not File");
            } else {
                if (file.delete()) {
                    sendStatus(200, "OK");
                } else {
                    sendStatus(400, "Cannot Delete File");
                }
            }
        } else {
            sendStatus(400, "File Not Found");
        }

        return Command.OK;
    }

    @Override
    public String getCommand() {
        return "rm";
    }
}
