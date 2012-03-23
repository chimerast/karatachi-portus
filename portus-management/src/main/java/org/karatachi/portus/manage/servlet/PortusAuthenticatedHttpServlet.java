package org.karatachi.portus.manage.servlet;

import java.io.IOException;
import java.security.cert.X509Certificate;
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

public class PortusAuthenticatedHttpServlet extends HttpServlet {
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
    protected final void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        if (!request.getScheme().equals("https")) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

        boolean authenticated = false;
        boolean bootstrap = false;

        X509Certificate certs[] =
                (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
        if (certs != null) {
            for (X509Certificate cert : certs) {
                if (cert.getSubjectDN().getName().indexOf("CN=portus-bootstrap") >= 0) {
                    authenticated = true;
                    bootstrap = true;
                    break;
                } else if (cert.getSubjectDN().getName().indexOf(
                        "CN=portus-node") >= 0) {
                    authenticated = true;
                    bootstrap = false;
                    break;
                }
            }
        }

        if (!authenticated) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        if (bootstrap) {
            doBootstrap(request, response);
        } else {
            doNode(request, response);
        }
    }

    protected void doBootstrap(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
    }

    protected void doNode(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
    }
}
