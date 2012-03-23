package org.karatachi.portus.node;

import java.io.IOException;

import org.karatachi.net.shell.Command;
import org.karatachi.net.shell.CommandShell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractCommand implements Command {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private CommandShell shell;

    @Override
    public int exec(CommandShell shell, String[] args) throws IOException {
        this.shell = shell;
        return command(args);
    }

    protected abstract int command(String[] args) throws IOException;

    protected final void sendTemporaryStatus(int status, String message)
            throws IOException {
        shell.sendTemporaryMessage(status + " " + message);
    }

    protected final void sendStatus(int status, String message)
            throws IOException {
        shell.sendMessage(status + " " + message);
    }

    protected final void sendHeader(String name, String value)
            throws IOException {
        shell.sendMessage(name + ": " + value);
    }

    protected final void sendHeader(String name, long value) throws IOException {
        shell.sendMessage(name + ": " + Long.toString(value));
    }

    protected final void sendSeparator() throws IOException {
        shell.sendMessage("");
    }

    protected final void sendBody(String body) throws IOException {
        shell.sendMessage(body);
    }
}
