package org.karatachi.portus.common.http;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.karatachi.classloader.PackageDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmbeddedPageResource {
    private static Logger logger =
            LoggerFactory.getLogger(EmbeddedPageResource.class);

    private static final Map<String, byte[]> EMBEDDED_PAGES;
    static {
        Map<String, byte[]> pages = new HashMap<String, byte[]>();
        for (String resource : new PackageDir("page").getResourceNames()) {
            try {
                byte[] data =
                        IOUtils.toByteArray(EmbeddedPageResource.class.getResourceAsStream("/page/"
                                + resource));
                pages.put(resource, data);
            } catch (Exception e) {
                logger.error("組み込みページロード失敗: " + resource);
            }
        }
        EMBEDDED_PAGES = pages;
    }
    private static final byte[] NO_PAGE =
            "<html><body>no page</body></html>".getBytes();

    public static byte[] getErrorPageInBytes(int sc) {
        return getPageInBytes(String.format("%d.html", sc));
    }

    public static byte[] getPageInBytes(String name) {
        byte[] data = EMBEDDED_PAGES.get(name);
        if (data != null) {
            return data;
        } else {
            return NO_PAGE;
        }
    }

    public static void responseErrorPage(HttpServletResponse response, int sc)
            throws IOException {
        response.setStatus(sc);
        response.setContentType("text/html; charset=UTF-8");
        EmbeddedPageResource.responseErrorPage(response.getOutputStream(), sc);
    }

    public static void responseErrorPage(OutputStream out, int sc)
            throws IOException {
        byte[] data = EMBEDDED_PAGES.get(String.format("%d.html", sc));
        if (data != null) {
            out.write(data);
        } else {
            out.write(NO_PAGE);
        }
        out.close();
    }

    public static void response(OutputStream out, String filename)
            throws IOException {
        byte[] data = EMBEDDED_PAGES.get(filename);
        if (data != null) {
            out.write(data);
        } else {
            out.write(NO_PAGE);
        }
        out.close();
    }
}
