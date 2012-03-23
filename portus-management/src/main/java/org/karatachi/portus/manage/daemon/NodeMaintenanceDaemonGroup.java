package org.karatachi.portus.manage.daemon;

import org.karatachi.portus.core.entity.NodeEvent;
import org.karatachi.portus.manage.node.task.NodeConsistencyTask;
import org.karatachi.translator.IntervalTranslator;

public class NodeMaintenanceDaemonGroup extends AbstractMaintenanceDaemonGroup {
    public NodeMaintenanceDaemonGroup() {
        super("NodeMaintenance", IntervalTranslator.min(5),
                IntervalTranslator.sec(10), IntervalTranslator.hour(6),
                NodeEvent.NODE_MAINTENANCE);
        tasks.add(NodeConsistencyTask.class);
    }
}
