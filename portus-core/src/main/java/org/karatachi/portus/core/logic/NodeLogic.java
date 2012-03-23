package org.karatachi.portus.core.logic;

import java.util.Date;
import java.util.List;

import org.karatachi.portus.core.dao.NodeDao;
import org.karatachi.portus.core.dao.NodeEventDao;
import org.karatachi.portus.core.entity.Node;
import org.karatachi.portus.core.entity.NodeEvent;
import org.seasar.framework.container.annotation.tiger.Binding;

public class NodeLogic {
    @Binding
    private NodeDao nodeDao;
    @Binding
    private NodeEventDao nodeEventDao;

    public List<Node> getActiveNodes() {
        return nodeDao.selectActive();
    }

    public List<Node> getNodesByEvent(String name) {
        return nodeDao.selectByEvent(name);
    }

    public Date getEvent(long nodeId, String name) {
        NodeEvent nodeEvent = nodeEventDao.selectByNode(nodeId, name);
        return nodeEvent != null ? nodeEvent.date : null;
    }

    public void updateEventTx(Node node, String name) {
        NodeEvent nodeEvent = nodeEventDao.selectByNode(node.id, name);
        if (nodeEvent != null) {
            nodeEvent.date = new Date();
            nodeEventDao.update(nodeEvent);
        } else {
            nodeEvent = new NodeEvent();
            nodeEvent.nodeId = node.id;
            nodeEvent.name = name;
            nodeEvent.date = new Date();
            nodeEventDao.insert(nodeEvent);
        }
    }
}
