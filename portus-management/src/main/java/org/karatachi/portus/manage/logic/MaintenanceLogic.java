package org.karatachi.portus.manage.logic;

import java.util.List;

import org.karatachi.portus.core.dao.NodeDao;
import org.karatachi.portus.core.dao.NodeEventDao;
import org.karatachi.portus.core.entity.Node;
import org.karatachi.portus.core.entity.NodeEvent;
import org.karatachi.portus.core.logic.NodeLogic;
import org.karatachi.portus.manage.service.NodeManagementService;
import org.seasar.framework.container.annotation.tiger.Binding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MaintenanceLogic {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Binding
    private NodeDao nodeDao;
    @Binding
    private NodeEventDao nodeEventDao;

    @Binding
    private NodeLogic nodeLogic;
    @Binding
    private NodeManagementService nodeManagementService;

    public void setNodeStatus(long nodeId, int status) {
        nodeDao.updateStatus(nodeId, status);
        nodeManagementService.updateNodeStatus(nodeId, status);
    }

    public void setNodeStatusForce(long nodeId, int status) {
        nodeDao.updateStatusForce(nodeId, status);
        nodeManagementService.updateNodeStatusForce(nodeId, status);
    }

    public List<Node> getActiveNodes() {
        return nodeDao.selectActive();
    }

    public long getNodeMaintainedTime(Node node, String eventName) {
        NodeEvent event = nodeEventDao.selectByNode(node.id, eventName);
        return event != null ? event.date.getTime() : 0L;
    }

    public void setNodeMaintainedTime(Node node, String eventName) {
        nodeLogic.updateEventTx(node, eventName);
    }
}
