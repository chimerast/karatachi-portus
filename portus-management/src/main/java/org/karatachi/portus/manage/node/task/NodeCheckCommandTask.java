package org.karatachi.portus.manage.node.task;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.SocketChannel;

import org.karatachi.net.shell.CommandHandler;
import org.karatachi.net.shell.CommandResponse;
import org.karatachi.portus.core.entity.Node;
import org.karatachi.portus.manage.logic.MaintenanceLogic;
import org.karatachi.portus.manage.node.NodeTask;
import org.karatachi.portus.manage.service.NodeManagementService;
import org.seasar.framework.container.annotation.tiger.Binding;

public class NodeCheckCommandTask extends NodeTask {
    @Binding
    private MaintenanceLogic maintenanceLogic;
    @Binding
    private NodeManagementService nodeManagementService;

    @Override
    public void execute(CommandHandler handler) throws IOException {
        CommandResponse response = handler.executeCommand("status 4");

        if (response == null
                || response.getStatusCode() != CommandResponse.S_OK) {
            return;
        }

        if (response.getHeaderLong("NodeId") != getNode().id) {
            maintenanceLogic.setNodeStatusForce(getNode().id,
                    Node.STATUS_REMOVED);
            return;
        }

        nodeManagementService.updateChassis(getNode(), response);

        if (response.getHeaderLong("FreeSpace") == 0) {
            maintenanceLogic.setNodeStatusForce(getNode().id,
                    Node.STATUS_NOT_WORKING);
            return;
        }

        /*
        if (response.getHeaderLong("Http-ActiveCount") > AssemblyInfo.OVERLOAD_THRESHOLD) {
            maintenanceLogic.setNodeStatus(getNode().id,
                    NodeTask.STATUS_OVERLOADED);
            return;
        }
        */

        if (getNode().httpPort != 0) {
            SocketChannel channel = SocketChannel.open();
            try {
                Socket socket = channel.socket();
                socket.connect(new InetSocketAddress(getNode().ipAddress,
                        getNode().httpPort), 2000);
                socket.setSoTimeout(2000);
                OutputStream out = socket.getOutputStream();
                out.write("GET / HTTP/1.0\r\n\r\n".getBytes());
                InputStream in = socket.getInputStream();
                in.read();
            } catch (IOException e) {
                if (e instanceof ClosedByInterruptException) {
                    throw e;
                }
                maintenanceLogic.setNodeStatus(getNode().id,
                        Node.STATUS_OVERLOADED);
                return;
            } finally {
                channel.close();
            }
        }

        maintenanceLogic.setNodeStatus(getNode().id, Node.STATUS_OK);
    }

    @Override
    public boolean isSupported(int revision) {
        return revision >= 2;
    }
}
