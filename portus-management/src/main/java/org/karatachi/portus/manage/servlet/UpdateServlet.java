package org.karatachi.portus.manage.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.karatachi.db.ConnectionWrapper;
import org.karatachi.portus.core.entity.NodeEvent;
import org.karatachi.portus.manage.AssemblyInfo;
import org.karatachi.portus.manage.update.SystemFile;

public class UpdateServlet extends PortusAuthenticatedHttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doBootstrap(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        long nodeId = NumberUtils.toLong(request.getHeader("Node-Id"));
        int nodeRevision =
                NumberUtils.toInt(request.getHeader("Node-Revision"));

        int update;

        ConnectionWrapper conn = getConnection();
        try {
            ResultSet rs =
                    conn.executeQuery(
                            "SELECT update FROM portus.node WHERE id=?", nodeId);
            if (rs.next()) {
                update = rs.getInt("update");
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            if (conn.executeUpdate(
                    "UPDATE portus.node_event SET date=CURRENT_TIMESTAMP WHERE node_id=? AND name=?",
                    nodeId, NodeEvent.NODE_UPDATE) == 0) {
                conn.executeUpdate(
                        "INSERT INTO portus.node_event(node_id, name, date) VALUES(?, ?, CURRENT_TIMESTAMP)",
                        nodeId, NodeEvent.NODE_UPDATE);
            }
        } catch (SQLException e) {
            logger.error("SQL実行例外によりシステムの更新に失敗", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        } finally {
            conn.dispose();
        }

        File basedir;
        switch (update) {
        case AssemblyInfo.UNSTABLE:
            basedir = new File(AssemblyInfo.PATH_NODE_UNSTABLE);
            break;
        case AssemblyInfo.TESTING:
            basedir = new File(AssemblyInfo.PATH_NODE_TESTING);
            break;
        default:
            basedir = new File(AssemblyInfo.PATH_NODE_STABLE);
            break;
        }

        SystemFile systemFile = SystemFile.newestSystemFile(basedir);

        boolean modified;
        if (systemFile == null) {
            modified = false;
        } else {
            modified = nodeRevision < systemFile.getRevision();
        }

        if (!modified) {
            response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }

        response.setIntHeader("Node-Revision", systemFile.getRevision());
        response.setHeader("Content-MD5", systemFile.getMD5());

        InputStream filein = systemFile.getFileInputStream();
        try {
            IOUtils.copy(filein, response.getOutputStream());
        } finally {
            filein.close();
        }
    }

    @Override
    protected void doNode(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        long nodeId = NumberUtils.toLong(request.getHeader("Node-Id"));
        int bootstrapRevision =
                NumberUtils.toInt(request.getHeader("Bootstrap-Revision"));

        ConnectionWrapper conn = getConnection();
        try {
            if (conn.executeUpdate(
                    "UPDATE portus.node_event SET date=CURRENT_TIMESTAMP WHERE node_id=? AND name=?",
                    nodeId, NodeEvent.BOOTSTRAP_UPDATE) == 0) {
                conn.executeUpdate(
                        "INSERT INTO portus.node_event(node_id, name, date) VALUES(?, ?, CURRENT_TIMESTAMP)",
                        nodeId, NodeEvent.BOOTSTRAP_UPDATE);
            }
        } catch (SQLException e) {
            logger.error("SQL実行例外によりシステムの更新に失敗", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        } finally {
            conn.dispose();
        }

        SystemFile systemFile =
                SystemFile.newestSystemFile(new File(
                        AssemblyInfo.PATH_BOOTSTRAP));

        boolean modified;
        if (systemFile == null) {
            modified = false;
        } else {
            modified = bootstrapRevision < systemFile.getRevision();
        }

        if (!modified) {
            response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }

        response.setIntHeader("Bootstrap-Revision", systemFile.getRevision());
        response.setHeader("Content-MD5", systemFile.getMD5());

        InputStream filein = systemFile.getFileInputStream();
        try {
            IOUtils.copy(filein, response.getOutputStream());
        } finally {
            filein.close();
        }
    }
}
