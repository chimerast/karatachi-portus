package org.karatachi.portus.manage.daemon.task;

import org.karatachi.daemon.producer.repeat.RepeatTask;
import org.karatachi.portus.core.entity.Node;

public class MaintenanceTask extends RepeatTask {
    private final Node chassis;

    public MaintenanceTask(Node node, long lastRunTime) {
        super(node.id, lastRunTime);
        this.chassis = node;
    }

    public Node getNode() {
        return chassis;
    }

    @Override
    public String toString() {
        return String.format("chassis: %s", chassis);
    }
}
