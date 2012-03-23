package org.karatachi.portus.node.command;

import java.io.IOException;
import java.lang.reflect.Method;

import org.karatachi.net.shell.Command;
import org.karatachi.portus.node.AbstractCommand;
import org.karatachi.portus.node.http.PortusHttpService;

public class SetCommand extends AbstractCommand {
    @Override
    protected int command(String[] args) throws IOException {
        if (args.length != 3) {
            return Command.INVALID_ARGUMENT_COUNTS;
        }

        String paramName = args[1];
        int value;
        try {
            value = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            return Command.INVALID_ARGUMENT;
        }

        if (paramName.equals("confirmInterval")) {
            setConfirmInterval(value);
        } else if (paramName.equals("sendBufferSize")) {
            PortusHttpService.sendBufferSize = value;
            sendStatus(200, "OK");
            sendHeader("SendBuffer-Size", value);
        } else if (paramName.equals("recvBufferSize")) {
            PortusHttpService.recvBufferSize = value;
            sendStatus(200, "OK");
            sendHeader("RecvBuffer-Size", value);
        } else {
            return Command.INVALID_ARGUMENT;
        }

        return Command.OK;
    }

    public void setConfirmInterval(int sec) throws IOException {
        // 最大値と最小値を限定
        if (sec < 60) {
            sec = 60;
        }
        if (sec > 2 * 60 * 60) {
            sec = 2 * 60 * 60;
        }

        try {
            Class<?> cls =
                    Class.forName("org.karatachi.portus.bootstrap.Bootstrap");
            Method method = cls.getMethod("setConfirmInterval", int.class);
            method.invoke(null, sec * 1000);

            sendStatus(200, "OK");
            sendHeader("Confirm-Interval", sec);
        } catch (Exception e) {
            sendStatus(400, "Could Not Setup CONFIRM_INTERVAL Field");
            sendHeader("Confirm-Interval", sec);
        }
    }

    @Override
    public String getCommand() {
        return "set";
    }
}
