package org.karatachi.portus.node.command;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.karatachi.net.shell.Command;
import org.karatachi.portus.node.AbstractCommand;
import org.karatachi.portus.node.AssemblyInfo;

public class CheckFileCommand extends AbstractCommand {
    @Override
    protected int command(String[] args) throws IOException {
        if (args.length != 2) {
            return Command.INVALID_ARGUMENT_COUNTS;
        }

        sendStatus(200, "OK");

        File file = new File(AssemblyInfo.PATH_RAW_DATA, args[1]);
        sendHeader("Exists", file.exists() ? "Yes" : "No");
        sendHeader("Is-File", file.isFile() ? "Yes" : "No");

        if (file.exists() && file.isFile()) {
            sendHeader("Can-Read", file.canRead() ? "Yes" : "No");
            sendHeader("Can-Write", file.canWrite() ? "Yes" : "No");
            sendHeader("Length", Long.toString(file.length()));

            FileInputStream in = null;
            try {
                in = new FileInputStream(file);
                in.read();
                sendHeader("Try-Read", "OK");
            } catch (IOException e) {
                sendHeader("Try-Read", e.getMessage());
            } finally {
                if (in != null) {
                    in.close();
                }
            }
        }

        return Command.OK;
    }

    @Override
    public String getCommand() {
        return "checkfile";
    }
}
