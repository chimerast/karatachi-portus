package org.karatachi.portus.node.command;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.karatachi.net.shell.Command;
import org.karatachi.portus.node.AbstractCommand;
import org.karatachi.portus.node.AssemblyInfo;

public class ListCommand extends AbstractCommand {
    @Override
    protected int command(String[] args) throws IOException {
        if (args.length != 2) {
            return Command.INVALID_ARGUMENT_COUNTS;
        }

        File dir = new File(AssemblyInfo.PATH_RAW_DATA);
        if (!dir.exists() || !dir.isDirectory()) {
            sendStatus(400, "Invalid Data Directory");
            return 1;
        }

        StringBuilder name = new StringBuilder(args[1]);
        if (name.length() > 6) {
            name.insert(6, '/');
        }
        if (name.length() > 4) {
            name.insert(4, '/');
        }
        if (name.length() > 2) {
            name.insert(2, '/');
        }

        if (name.indexOf(".") != -1) {
            sendStatus(400, "Invalid Path");
            return Command.OK;
        }

        sendStatus(200, "OK");
        sendSeparator();

        dir = new File(AssemblyInfo.PATH_RAW_DATA, name.toString());
        if (dir.exists() && dir.isDirectory()) {
            listFiles(dir, args[1]);
        }

        return Command.OK;
    }

    private void listFiles(File dir, String prefix) throws IOException {
        File[] files = dir.listFiles();
        Arrays.sort(files);
        for (File file : files) {
            if (file.isDirectory()) {
                listFiles(file, prefix + file.getName());
            } else if (!file.isFile()) {
                continue;
            } else {
                sendBody(String.format("%s,%s", prefix + file.getName(),
                        file.length()));
            }
        }
    }

    @Override
    public String getCommand() {
        return "ls";
    }
}
