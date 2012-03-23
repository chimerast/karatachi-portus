package org.karatachi.portus.manage.daemon;

import org.karatachi.portus.core.entity.NodeEvent;
import org.karatachi.portus.manage.node.task.NodeCheckCommandTask;
import org.karatachi.translator.IntervalTranslator;

public class NodeCheckDaemonGroup extends AbstractMaintenanceDaemonGroup {
    public NodeCheckDaemonGroup() {
        super("NodeCheck", IntervalTranslator.min(1),
                IntervalTranslator.sec(10), IntervalTranslator.min(1),
                NodeEvent.NODE_CHECK);
        tasks.add(NodeCheckCommandTask.class);
    }
}
