package org.karatachi.portus.manage.daemon;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.karatachi.daemon.Daemon;
import org.karatachi.daemon.producer.repeat.RepeatDaemonGroup;
import org.karatachi.daemon.producer.repeat.RepeatTask;
import org.karatachi.portus.core.entity.Node;
import org.karatachi.portus.manage.daemon.task.MaintenanceTask;
import org.karatachi.portus.manage.logic.MaintenanceLogic;
import org.karatachi.portus.manage.node.NodeHandler;
import org.karatachi.portus.manage.node.NodeTask;
import org.seasar.framework.container.annotation.tiger.Binding;

public class AbstractMaintenanceDaemonGroup extends RepeatDaemonGroup {
    private final String eventName;

    protected final List<Class<? extends NodeTask>> tasks;

    @Binding
    private MaintenanceLogic maintenanceLogic;

    public AbstractMaintenanceDaemonGroup(String name, long producerInterval,
            long workerInterval, long taskInterval, String eventName) {
        super(name, producerInterval, workerInterval, taskInterval);

        this.eventName = eventName;
        this.tasks = new ArrayList<Class<? extends NodeTask>>();
    }

    protected boolean filter(Node chassis) {
        return true;
    }

    @Override
    protected final List<? extends RepeatTask> replaceTask() {
        List<RepeatTask> task = new LinkedList<RepeatTask>();

        List<Node> nodes = maintenanceLogic.getActiveNodes();
        for (Node node : nodes) {
            if (!filter(node)) {
                continue;
            }
            task.add(new MaintenanceTask(node,
                    maintenanceLogic.getNodeMaintainedTime(node, eventName)));
        }
        return task;
    }

    @Override
    protected final void work(Daemon worker, RepeatTask task) throws Exception {
        Node node = ((MaintenanceTask) task).getNode();

        worker.setDetailText("Work on: %s", node);
        try {
            if (eventName != null) {
                maintenanceLogic.setNodeMaintainedTime(node, eventName);
            }

            NodeHandler handler = NodeHandler.createHandler(node);
            try {
                handler.executeDDDTask(tasks);
            } finally {
                handler.close();
            }
        } catch (SocketTimeoutException e) {
            logger.debug("Socket timeout: {}", node);
            maintenanceLogic.setNodeStatus(node.id, Node.STATUS_DOWN);
        } catch (ConnectException e) {
            logger.debug("Connection failed: {}", node);
            maintenanceLogic.setNodeStatus(node.id, Node.STATUS_DOWN);
        } catch (IOException e) {
            logger.error("I/O error: " + node, e);
        }
    }
}
