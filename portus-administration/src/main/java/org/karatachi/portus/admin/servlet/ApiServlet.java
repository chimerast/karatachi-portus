package org.karatachi.portus.admin.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.karatachi.portus.batch.logic.PublishFileLogic;
import org.karatachi.portus.batch.logic.RegisterFileLogic;
import org.seasar.framework.container.SingletonS2Container;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            String servletPath =
                    request.getContextPath() + request.getServletPath();
            if (request.getRequestURI().startsWith(servletPath + "/register")) {
                servletPath += "/register";
                String source =
                        request.getRequestURI().substring(servletPath.length());
                RegisterFileLogic fileRegisterLogic =
                        SingletonS2Container.getComponent(RegisterFileLogic.class);
                fileRegisterLogic.registerFile(source);
                response.getWriter().println("OK");
            } else if (request.getRequestURI().startsWith(
                    servletPath + "/publish")) {
                servletPath += "/publish";
                String source =
                        request.getRequestURI().substring(servletPath.length());
                PublishFileLogic filePublishLogic =
                        SingletonS2Container.getComponent(PublishFileLogic.class);
                filePublishLogic.publishFile(source);
                response.getWriter().println("OK");
            } else {
                response.getWriter().println(
                        "requestURI=" + request.getRequestURI());
                response.getWriter().println("servletPath=" + servletPath);
            }
        } catch (Exception e) {
            response.getWriter().println("NG");
            logger.error("ファイルエラー", e);
        }
    }
}
