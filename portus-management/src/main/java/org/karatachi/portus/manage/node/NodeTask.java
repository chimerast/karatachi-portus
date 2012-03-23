package org.karatachi.portus.manage.node;

import org.karatachi.net.shell.CommandTask;
import org.karatachi.portus.core.entity.Node;

public abstract class NodeTask extends CommandTask {
    private Node node;

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public abstract boolean isSupported(int revision);
}
