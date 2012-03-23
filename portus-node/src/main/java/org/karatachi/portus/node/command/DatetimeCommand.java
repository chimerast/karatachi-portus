package org.karatachi.portus.node.command;

import java.io.IOException;
import java.util.Date;

import org.karatachi.net.shell.Command;
import org.karatachi.portus.node.AbstractCommand;
import org.karatachi.proc.NativeExecutor;

public class DatetimeCommand extends AbstractCommand {
    @Override
    protected int command(String[] args) throws IOException {
        if (args.length != 1 && args.length != 2) {
            return Command.INVALID_ARGUMENT_COUNTS;
        }

        boolean succeeded = true;

        if (args.length == 2) {
            Date date;
            try {
                date = new Date(Long.parseLong(args[1]));

                NativeExecutor executor = new NativeExecutor();
                succeeded &= executor.exec("cmd /C date %tF", date) == 0;
                succeeded &= executor.exec("cmd /C time %tT", date) == 0;
            } catch (Exception e) {
                return Command.INVALID_ARGUMENT;
            }
        }

        if (succeeded) {
            sendStatus(200, "OK");
        } else {
            sendStatus(400, "Could not setup datetime.");
        }

        Date now = new Date();
        sendHeader("Datetime", String.format("%1$tF %1tT", now));
        sendHeader("Datetime-Long", now.getTime());
        return Command.OK;
    }

    @Override
    public String getCommand() {
        return "datetime";
    }
}
