package org.karatachi.portus.manage.servlet;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.math.NumberUtils;
import org.karatachi.db.ConnectionWrapper;

public class RegisterServlet extends PortusAuthenticatedHttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doBootstrap(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        long nodeId = NumberUtils.toLong(request.getHeader("Node-Id"));
        int bootstrapRevision =
                NumberUtils.toInt(request.getHeader("Bootstrap-Revision"));
        String status = request.getHeader("Status");

        ConnectionWrapper conn = getConnection();
        try {
            if (status.equalsIgnoreCase("Regist")) {
                // statusは変更しない
                int ret =
                        conn.executeUpdate(
                                "UPDATE node SET ip_address=?, bootstrap_revision=? WHERE id=?",
                                request.getRemoteAddr(), bootstrapRevision,
                                nodeId);
                if (ret == 0) {
                    conn.executeUpdate(
                            "INSERT INTO node(id, node_block_id, ip_address, bootstrap_revision) VALUES(?, ?, ?, ?)",
                            nodeId, nodeId, request.getRemoteAddr(),
                            bootstrapRevision);
                }
            } else if (status.equalsIgnoreCase("Shutdown")) {
                conn.executeUpdate(
                        "UPDATE node SET status=0 WHERE id=? AND status <= 2",
                        nodeId);
            }
        } catch (SQLException e) {
            logger.error("登録エラー", e);
        } finally {
            conn.dispose();
        }
    }

    @Override
    protected void doNode(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        String ipAddress = request.getRemoteAddr();
        long nodeId = NumberUtils.toLong(request.getHeader("Node-Id"));
        int ctrlPort = NumberUtils.toInt(request.getHeader("Ctrl-Port"));
        int httpPort = NumberUtils.toInt(request.getHeader("Http-Port"));
        int nodeRevision =
                NumberUtils.toInt(request.getHeader("Node-Revision"));
        int protocolRevision =
                NumberUtils.toInt(request.getHeader("Protocol-Revision"));
        String status = request.getHeader("Status");

        ConnectionWrapper conn = getConnection();
        try {
            if ("HostUpdate".equalsIgnoreCase(status)) {
                conn.executeUpdate(
                        "UPDATE node SET ip_address=?, ctrl_port=?, http_port=?, node_revision=?, protocol_revision=? WHERE id=? AND status <= 2",
                        ipAddress, ctrlPort, httpPort, nodeRevision,
                        protocolRevision, nodeId);
            } else {
                // ポート登録後ルータの再起動を行うためstatus = 0
                conn.executeUpdate(
                        "UPDATE node SET status=0, ip_address=?, ctrl_port=?, http_port=?, node_revision=?, protocol_revision=? WHERE id=? AND status <= 2",
                        ipAddress, ctrlPort, httpPort, nodeRevision,
                        protocolRevision, nodeId);
            }

            String hostname;
            try {
                hostname =
                        InetAddress.getByName(request.getRemoteAddr()).getCanonicalHostName();
            } catch (UnknownHostException e) {
                hostname = request.getRemoteAddr();
            }
            response.setHeader("Accessed-Host", hostname);

            response.getWriter().print("OK");
        } catch (Exception e) {
            logger.error("登録エラー", e);
        } finally {
            conn.dispose();
        }
    }
}
