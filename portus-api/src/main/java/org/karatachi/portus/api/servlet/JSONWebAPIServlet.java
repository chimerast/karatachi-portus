package org.karatachi.portus.api.servlet;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.commons.io.IOUtils;
import org.karatachi.classloader.PackageDir;
import org.karatachi.portus.api.WebGetAPI;
import org.karatachi.portus.api.WebPostAPI;
import org.karatachi.portus.api.dao.SSLCrlDao;
import org.karatachi.portus.api.type.ResponseContainer;
import org.karatachi.portus.core.AssemblyInfo;
import org.karatachi.portus.core.PortusRuntimeException;
import org.karatachi.portus.core.dto.ValidationSettingsDto;
import org.karatachi.portus.core.logic.AuthorizationLogic;
import org.seasar.framework.container.SingletonS2Container;
import org.seasar.framework.exception.SRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSONWebAPIServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<String, Class<? extends WebGetAPI>> getApiMap =
            new HashMap<String, Class<? extends WebGetAPI>>();
    private final Map<String, Class<? extends WebPostAPI>> postApiMap =
            new HashMap<String, Class<? extends WebPostAPI>>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        PackageDir dir =
                new PackageDir(config.getInitParameter("rootPackageName"));
        {
            List<Class<? extends WebGetAPI>> apis =
                    dir.getClasses(WebGetAPI.class);
            for (Class<? extends WebGetAPI> api : apis) {
                try {
                    getApiMap.put(api.newInstance().getName(), api);
                } catch (InstantiationException e) {
                    logger.warn("API initialization error.", e);
                } catch (IllegalAccessException e) {
                    logger.warn("API initialization error.", e);
                } catch (SRuntimeException e) {
                    logger.warn("API initialization error.", e);
                }
            }
        }
        {
            List<Class<? extends WebPostAPI>> apis =
                    dir.getClasses(WebPostAPI.class);
            for (Class<? extends WebPostAPI> api : apis) {
                try {
                    postApiMap.put(api.newInstance().getName(), api);
                } catch (InstantiationException e) {
                    logger.warn("API initialization error.", e);
                } catch (IllegalAccessException e) {
                    logger.warn("API initialization error.", e);
                } catch (SRuntimeException e) {
                    logger.warn("API initialization error.", e);
                }
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        doExecute(request, response);
    }

    @Override
    protected final void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        doExecute(request, response);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected void doExecute(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain");

        if (!request.getScheme().equals("https")) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

        SSLCrlDao sslCrlDao =
                SingletonS2Container.getComponent(SSLCrlDao.class);

        boolean authenticated = false;

        String cn = null;
        X509Certificate[] certs =
                (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
        if (certs != null) {
            for (X509Certificate cert : certs) {
                if (cert.getSerialNumber() == null
                        || sslCrlDao.containsSerial(cert.getSerialNumber().longValue()) != 0) {
                    authenticated = false;
                    break;
                }
                if (cert.getSubjectDN().getName().indexOf("CN=") >= 0) {
                    authenticated = true;
                    cn = cert.getSubjectDN().getName();
                    break;
                }
            }
        }

        String username;
        try {
            logger.debug("CN: " + cn);
            String[] data = cn.split("=");
            String[] userinfo = data[1].split("!");
            logger.debug("SERVICE_NAME: " + userinfo[0] + " & "
                    + AssemblyInfo.SERVICE_NAME);
            if (!userinfo[0].equals(AssemblyInfo.SERVICE_NAME)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            username = userinfo[1];
            logger.debug("USERNAME: " + username);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        AuthorizationLogic authLogic =
                SingletonS2Container.getComponent(AuthorizationLogic.class);
        authenticated &= authLogic.authenticateWithoutPassword(username);

        if (!authenticated) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        ResponseContainer res = new ResponseContainer();
        try {
            String uri;
            try {
                uri =
                        request.getRequestURI().substring(
                                request.getContextPath().length()
                                        + request.getServletPath().length() + 1);
            } catch (StringIndexOutOfBoundsException e) {
                throw new PortusRuntimeException("指定されたAPIは存在しません。",
                        HttpURLConnection.HTTP_BAD_METHOD);
            }

            String method, path;
            if (uri.indexOf('/') != -1) {
                method = uri.substring(0, uri.indexOf('/'));
                path = uri.substring(uri.indexOf('/'));
                path = URLDecoder.decode(path, "UTF-8");
            } else {
                method = uri;
                path = null;
            }

            ValidationSettingsDto validationSettingsDto =
                    SingletonS2Container.getComponent(ValidationSettingsDto.class);
            validationSettingsDto.relaxFilenameValidation = true;

            if (getApiMap.containsKey(method)) {
                WebGetAPI api =
                        SingletonS2Container.getComponent(getApiMap.get(method));
                Map params = request.getParameterMap();
                Object ret = api.exec(path, params, request.getSession());
                res.setSuccess(true);
                res.setCode(HttpURLConnection.HTTP_OK);
                res.setResponse(ret);
            } else if (postApiMap.containsKey(method)) {
                WebPostAPI api =
                        SingletonS2Container.getComponent(postApiMap.get(method));
                String body = IOUtils.toString(request.getInputStream());
                Map params = request.getParameterMap();
                Object ret = api.exec(path, params, body, request.getSession());
                res.setSuccess(true);
                res.setCode(HttpURLConnection.HTTP_OK);
                res.setResponse(ret);
            } else {
                throw new PortusRuntimeException("指定されたAPIは存在しません。",
                        HttpURLConnection.HTTP_BAD_METHOD);
            }
        } catch (PortusRuntimeException e) {
            res.setSuccess(false);
            res.setCode(e.getCode());
            res.setResponse(e.getMessage());
            logger.error("portus error.", e);
        } catch (Exception e) {
            res.setSuccess(false);
            res.setCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
            res.setResponse(e.getMessage());
            logger.error("internal error.", e);
        }

        response.getWriter().println(JSONObject.fromObject(res));
    }
}
