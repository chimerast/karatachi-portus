package org.karatachi.portus.manage.node;

import java.io.IOException;
import java.util.List;

import org.karatachi.net.shell.CommandHandler;
import org.karatachi.net.shell.CommandResponse;
import org.karatachi.net.shell.CommandResponseCallback;
import org.karatachi.portus.core.entity.Node;
import org.seasar.framework.container.SingletonS2Container;

public abstract class NodeHandler extends CommandHandler {
    private final Node chassis;

    protected NodeHandler(Node chassis) {
        this.chassis = chassis;
    }

    public final void executeDDDTask(NodeTask task) throws IOException {
        if (!task.isSupported(chassis.protocolRevision)) {
            return;
        }
        task.setNode(chassis);
        task.execute(this);
    }

    public final void executeDDDTask(List<Class<? extends NodeTask>> tasks)
            throws IOException, InstantiationException, IllegalAccessException {
        for (Class<? extends NodeTask> clazz : tasks) {
            executeDDDTask(SingletonS2Container.getComponent(clazz));
        }
    }

    @Override
    public abstract CommandResponse executeCommand(String command,
            CommandResponseCallback callback) throws IOException;

    @Override
    public abstract void close() throws IOException;

    public static NodeHandler createHandler(Node chassis)
            throws IOException {
        return new RshNodeHandler(chassis);
    }
}
