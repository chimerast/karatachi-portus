package org.karatachi.portus.manage.servlet;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.karatachi.db.ConnectionWrapper;
import org.seasar.framework.container.SingletonS2Container;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatusServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final ConnectionWrapper getConnection() throws ServletException {
        try {
            DataSource dataSource =
                    SingletonS2Container.getComponent(DataSource.class);
            return new ConnectionWrapper(dataSource.getConnection());
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            long nodeId = Long.parseLong(request.getParameter("node_id"), 16);

            ConnectionWrapper conn = getConnection();
            try {
                ResultSet rs =
                        conn.executeQuery(
                                "SELECT status FROM portus.node WHERE id=?",
                                nodeId);
                if (rs.next()) {
                    response.getWriter().println(rs.getInt("status"));
                } else {
                    response.getWriter().println(-1);
                }
            } finally {
                conn.dispose();
            }
        } catch (Exception e) {
            response.getWriter().println(-2);
        }
    }
}
