package org.karatachi.portus.distribute;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.karatachi.portus.common.crypto.EncryptionUtils;
import org.karatachi.portus.common.http.EmbeddedPageResource;
import org.karatachi.portus.common.net.AccessInfo;
import org.karatachi.portus.core.AssemblyInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        AccessInfo accessInfo;
        try {
            String code = request.getParameter("x");
            accessInfo = AccessInfo.decrypt(code);
            if (accessInfo == null) {
                EmbeddedPageResource.responseErrorPage(response,
                        HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        } catch (Exception e) {
            // アクセスコード違反
            EmbeddedPageResource.responseErrorPage(response,
                    HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        if (new Date().getTime() > accessInfo.expire) {
            // 有効期限切れ
            EmbeddedPageResource.responseErrorPage(response,
                    HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        if (!accessInfo.ipAddress.equals("0.0.0.0")) {
            // アクセス元違反
            EmbeddedPageResource.responseErrorPage(response,
                    HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        File file =
                new File(AssemblyInfo.PATH_REG_DATA,
                        EncryptionUtils.toFilePath(accessInfo.fileId));
        if (!file.exists() || !file.canRead() || file.isDirectory()) {
            // ファイル存在せず
            EmbeddedPageResource.responseErrorPage(response,
                    HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader("Content-Length", Long.toString(file.length()));
        response.setHeader("Content-Disposition",
                String.format("inline; filename=\"%s\"", accessInfo.fileName));

        FileInputStream in = new FileInputStream(file);
        try {
            IOUtils.copy(in, response.getOutputStream());
        } finally {
            in.close();
        }
    }
}
