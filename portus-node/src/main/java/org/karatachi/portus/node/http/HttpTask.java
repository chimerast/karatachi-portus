package org.karatachi.portus.node.http;

import java.io.IOException;
import java.net.Socket;

import org.apache.http.ConnectionClosedException;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.karatachi.portus.node.AssemblyInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpTask implements Runnable, HttpRequestInterceptor,
        HttpResponseInterceptor {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final HttpRequestHandlerRegistry registry;
    private final HttpService httpservice;
    private final DefaultHttpServerConnection conn;

    public HttpTask(Socket socket) throws IOException {
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setSoTimeout(params, PortusHttpService.soTimeout);
        HttpConnectionParams.setTcpNoDelay(params, true);
        HttpConnectionParams.setStaleCheckingEnabled(params, false);

        params.setParameter(HttpProtocolParams.ORIGIN_SERVER,
                AssemblyInfo.getServiceVersion());

        BasicHttpProcessor httpproc = new BasicHttpProcessor();
        httpproc.addInterceptor(new ResponseDate());
        httpproc.addInterceptor(new ResponseServer());
        // HEADでContent-Lengthが正常に返らないためコメントアウト
        // httpproc.addInterceptor(new ResponseContent());

        if (logger.isDebugEnabled()) {
            httpproc.addRequestInterceptor(this);
            httpproc.addResponseInterceptor(this);
        }

        registry = new HttpRequestHandlerRegistry();
        registry.register("*", new HttpFileHandler());

        httpservice =
                new HttpService(httpproc, new DefaultConnectionReuseStrategy(),
                        new DefaultHttpResponseFactory(), registry, params);

        conn = new DefaultHttpServerConnection();
        conn.bind(socket, params);
    }

    public void run() {
        HttpContext context = new BasicHttpContext(null);
        try {
            httpservice.handleRequest(conn, context);
        } catch (ConnectionClosedException ex) {
            logger.info(logHeader() + "Client closed connection", ex);
        } catch (IOException ex) {
            logger.error(logHeader() + "I/O error: " + ex.getMessage(), ex);
        } catch (HttpException ex) {
            logger.error(logHeader()
                    + "Unrecoverable HTTP protocol violation: ", ex);
        } finally {
            try {
                this.conn.close();
            } catch (IOException ignore) {
            }
        }
    }

    private String logHeader() {
        return "[" + conn.getRemoteAddress().getHostAddress() + ":"
                + conn.getRemotePort() + "] ";
    }

    @Override
    public void process(HttpRequest request, HttpContext context)
            throws HttpException, IOException {
        logger.debug(logHeader() + "----");
        logger.debug(logHeader() + request.getRequestLine().toString());
        for (Header header : request.getAllHeaders()) {
            logger.debug(logHeader()
                    + String.format("> %s: %s", header.getName(),
                            header.getValue()));
        }
    }

    @Override
    public void process(HttpResponse response, HttpContext context)
            throws HttpException, IOException {
        logger.debug(logHeader() + response.getStatusLine().toString());
        for (Header header : response.getAllHeaders()) {
            logger.debug(logHeader()
                    + String.format("< %s: %s", header.getName(),
                            header.getValue()));
        }
    }

    public void rejectConnection() {
        registry.register("*", new HttpRejectHandler());
        run();
    }
}
