package org.karatachi.portus.node.command;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.karatachi.net.shell.Command;
import org.karatachi.portus.node.AbstractCommand;
import org.karatachi.portus.node.AssemblyInfo;

public class PrintlogCommand extends AbstractCommand {
    @Override
    protected int command(String[] args) throws IOException {
        if (args.length == 1) {
            return Command.INVALID_ARGUMENT_COUNTS;
        }

        String logname = AssemblyInfo.PATH_LOG + args[1] + "/status.log";

        int maxline = 10;
        if (args.length > 2) {
            try {
                maxline = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                return Command.INVALID_ARGUMENT;
            }
        }

        File log = new File(logname);
        if (!log.exists() || log.isDirectory()) {
            sendStatus(400, "Logfile Not Found");
            return 1;
        }

        if (!log.canRead()) {
            sendStatus(400, "Cannot Read Logfile");
            return 1;
        }

        List<String> lines = new ArrayList<String>();

        BufferedReader in = new BufferedReader(new FileReader(log));

        String line;
        while ((line = in.readLine()) != null) {
            lines.add(line);
        }

        sendStatus(200, "OK");
        sendSeparator();

        if (maxline > lines.size()) {
            maxline = lines.size();
        }

        while (maxline > 0) {
            sendBody(lines.get(lines.size() - maxline));
            --maxline;
        }

        return Command.OK;
    }

    @Override
    public String getCommand() {
        return "printlog";
    }
}
