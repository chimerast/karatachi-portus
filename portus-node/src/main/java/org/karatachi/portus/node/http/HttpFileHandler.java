package org.karatachi.portus.node.http;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpInetConnection;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.FileEntity;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.karatachi.portus.common.crypto.EncryptionUtils;
import org.karatachi.portus.common.http.EmbeddedPageResource;
import org.karatachi.portus.common.net.AccessInfo;
import org.karatachi.portus.node.AssemblyInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpFileHandler implements HttpRequestHandler {
    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void handle(final HttpRequest request, final HttpResponse response,
            final HttpContext context) throws HttpException, IOException {
        String method = request.getRequestLine().getMethod().toUpperCase();
        if (!method.equals("GET") && !method.equals("HEAD")) {
            errorResponse(response, HttpStatus.SC_NOT_IMPLEMENTED);
            return;
        }

        if ("/crossdomain.xml".equals(request.getRequestLine().getUri())) {
            crossdomainResponse(response);
            return;
        }

        AccessInfo accessInfo;
        try {
            String uri = request.getRequestLine().getUri();
            int idx = uri.indexOf('?');
            if (idx == -1) {
                redirectResponse(request, response, uri);
                return;
            }

            String code = null;
            String query = uri.substring(idx + 1);
            for (String param : query.split("&")) {
                if (param.startsWith("x=")) {
                    code = param.substring(2);
                }
            }

            if (code == null) {
                redirectResponse(request, response, uri.substring(0, idx));
                return;
            }

            accessInfo = AccessInfo.decrypt(code);
            if (accessInfo == null) {
                errorResponse(response, HttpStatus.SC_UNAUTHORIZED);
                return;
            }
        } catch (Exception e) {
            // アクセスコード違反
            errorResponse(response, HttpStatus.SC_UNAUTHORIZED);
            return;
        }

        if (new Date().getTime() > accessInfo.expire) {
            // 有効期限切れ
            errorResponse(response, HttpStatus.SC_FORBIDDEN);
            return;
        }

        String remoteIpAddress = null;
        {
            Object attribute;
            if ((attribute =
                    context.getAttribute(ExecutionContext.HTTP_CONNECTION)) != null) {
                HttpInetConnection conn = (HttpInetConnection) attribute;
                remoteIpAddress = conn.getRemoteAddress().getHostAddress();
            }
        }

        if (!accessInfo.ipAddress.equals("0.0.0.0")
                && !accessInfo.ipAddress.equals(remoteIpAddress)) {
            // アクセス元違反
            errorResponse(response, HttpStatus.SC_FORBIDDEN);
            return;
        }

        File file =
                new File(AssemblyInfo.PATH_RAW_DATA,
                        EncryptionUtils.toFilePathWithExtension(
                                accessInfo.fileId, accessInfo.fileName));
        if (!file.exists() || !file.canRead() || file.isDirectory()) {
            file =
                    new File(AssemblyInfo.PATH_RAW_DATA,
                            EncryptionUtils.toFilePath(accessInfo.fileId));
            if (!file.exists() || !file.canRead() || file.isDirectory()) {
                // ファイル存在せず
                errorResponse(response, HttpStatus.SC_NOT_FOUND);
                return;
            }
        }

        Header range = request.getFirstHeader("Range");
        long range_from = -1, range_to = -1;
        if (range != null && range.getValue() != null) {
            String[] pair = range.getValue().split("=");
            if (pair.length == 2 && pair[0].trim().equalsIgnoreCase("bytes")) {
                String[] from_to = pair[1].trim().split("-");
                if (from_to.length == 1) {
                    try {
                        range_from = Long.parseLong(from_to[0].trim());
                        range_to = file.length() - 1;

                        if (range_from >= file.length()) {
                            range_from = -1;
                        }
                        if (range_from > range_to) {
                            range_from = -1;
                        }
                    } catch (NumberFormatException e) {
                        range_from = -1;
                    }
                } else if (from_to.length == 2) {
                    try {
                        range_from = Long.parseLong(from_to[0].trim());
                        range_to = Long.parseLong(from_to[1].trim());

                        if (range_from >= file.length()) {
                            range_from = -1;
                        }
                        if (range_to >= file.length()) {
                            range_to = file.length() - 1;
                        }
                        if (range_from > range_to) {
                            range_from = -1;
                        }
                    } catch (NumberFormatException e) {
                        range_from = -1;
                    }
                }
            }
        }

        String contentType =
                HttpFileContentType.getContentType(accessInfo.fileName);

        response.setHeader("Accept-Ranges", "bytes");

        if (accessInfo.nocache) {
            response.setHeader("Cache-Control",
                    "no-store, no-cache, must-revalidate, post-check=0, pre-check=0");
            response.setHeader("Pragma", "no-cache");
        }

        DateFormat dateformatter =
                new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz",
                        Locale.ENGLISH);
        dateformatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        response.setHeader("Last-Modified",
                dateformatter.format(new Date(file.lastModified())));

        response.setHeader(
                "ETag",
                String.format("\"%s-%s-%s\"", accessInfo.fileId,
                        file.lastModified(), file.length()));

        HttpEntity body;
        if (range_from == -1) {
            response.setStatusCode(HttpStatus.SC_OK);
            body = new FileEntity(file, contentType);
        } else {
            response.setStatusCode(HttpStatus.SC_PARTIAL_CONTENT);
            response.setHeader(
                    "Content-Range",
                    String.format("bytes %d-%d/%d", range_from, range_to,
                            file.length()));
            body =
                    new PartialFileEntity(file, contentType, range_from,
                            range_to);
        }

        response.setHeader("Content-Type", contentType);
        response.setHeader("Content-Length",
                Long.toString(body.getContentLength()));
        response.setHeader("Connection", "Close");

        if (method.equals("GET")) {
            response.setEntity(body);
        }
    }

    private void redirectResponse(HttpRequest request, HttpResponse response,
            String url) {
        if (url.startsWith("/~")) {
            url = "http://" + url.substring(2);
        } else {
            // 絶対パスのリンクの場合はrefererをみてパスを生成する
            Header referer = request.getFirstHeader("Referer");
            if (referer == null || referer.getValue() == null) {
                errorResponse(response, HttpStatus.SC_FORBIDDEN);
                return;
            }

            String domain = referer.getValue();

            int start = domain.indexOf("/~");
            int end = domain.indexOf('/', start + 1);
            if (start == -1 || end == -1) {
                errorResponse(response, HttpStatus.SC_FORBIDDEN);
                return;
            }
            url = "http://" + domain.substring(start + 2, end) + url;
        }

        response.setStatusCode(HttpStatus.SC_MOVED_TEMPORARILY);
        response.addHeader("Location", url);
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

    private void crossdomainResponse(HttpResponse response) {
        response.setStatusCode(HttpStatus.SC_OK);
        ByteArrayEntity body =
                new ByteArrayEntity(
                        EmbeddedPageResource.getPageInBytes("crossdomain.xml"));
        response.setHeader("Content-Type", "application/xml; charset=UTF-8");
        response.setHeader("Content-Length",
                Long.toString(body.getContentLength()));
        response.setHeader("Connection", "Close");
        response.setEntity(body);
    }
}
