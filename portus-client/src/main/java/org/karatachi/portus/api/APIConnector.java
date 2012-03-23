package org.karatachi.portus.api;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSON;
import net.sf.json.JSONObject;

import org.apache.commons.io.IOUtils;
import org.karatachi.portus.api.type.ResponseContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class APIConnector {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final String baseUrl;
    protected final String domain;

    public APIConnector(String baseUrl, String domain) {
        this.baseUrl = baseUrl;
        this.domain = domain;
    }

    protected final ResponseContainer doGetRequest(String method, String path,
            Class<?> responseClass) throws IOException {
        logger.info("connect to : " + baseUrl + "/" + method + "/"
                + normalizePath(path));
        HttpURLConnection conn =
                (HttpURLConnection) new URL(baseUrl + "/" + method + "/"
                        + normalizePath(path)).openConnection();
        try {
            conn.setRequestMethod("GET");
            conn.connect();

            Map<String, Class<?>> classmap = new HashMap<String, Class<?>>();
            classmap.put("response", responseClass);
            return (ResponseContainer) JSONObject.toBean(
                    JSONObject.fromObject(IOUtils.toString(conn.getInputStream())),
                    ResponseContainer.class, classmap);
        } finally {
            conn.disconnect();
        }
    }

    protected final ResponseContainer doPostRequest(String method, JSON json,
            Class<?> responseClass) throws IOException {
        logger.info("connect to : " + baseUrl + "/" + method);
        HttpURLConnection conn =
                (HttpURLConnection) new URL(baseUrl + "/" + method).openConnection();
        try {
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            os.write(json.toString().getBytes());
            os.close();

            Map<String, Class<?>> classmap = new HashMap<String, Class<?>>();
            classmap.put("response", responseClass);
            return (ResponseContainer) JSONObject.toBean(
                    JSONObject.fromObject(IOUtils.toString(conn.getInputStream())),
                    ResponseContainer.class, classmap);
        } finally {
            conn.disconnect();
        }
    }

    protected final String normalizePath(String path) {
        path = path.replace("//", "/");
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return path;
    }
}
