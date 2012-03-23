package org.karatachi.portus.node.http;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.karatachi.portus.common.http.EmbeddedPageResource;

public class HttpRejectHandler implements HttpRequestHandler {
    public void handle(final HttpRequest request, final HttpResponse response,
            final HttpContext context) throws HttpException, IOException {
        String method = request.getRequestLine().getMethod().toUpperCase();
        if (!method.equals("GET") && !method.equals("HEAD")) {
            errorResponse(response, HttpStatus.SC_NOT_IMPLEMENTED);
        } else {
            errorResponse(response, HttpStatus.SC_SERVICE_UNAVAILABLE);
        }
    }

    private void errorResponse(HttpResponse response, int sc) {
        response.setStatusCode(sc);
        ByteArrayEntity body =
                new ByteArrayEntity(
                        EmbeddedPageResource.getErrorPageInBytes(sc));
        response.setHeader("Content-Type", "text/html; charset=UTF-8");
        response.setHeader("Content-Length",
                Long.toString(body.getContentLength()));
        response.setHeader("Connection", "Close");
        response.setEntity(body);
    }
}
