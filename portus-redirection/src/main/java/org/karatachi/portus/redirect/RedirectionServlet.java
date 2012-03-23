package org.karatachi.portus.redirect;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.karatachi.db.ConnectionManager;
import org.karatachi.db.DataSourceManager;
import org.karatachi.portus.common.crypto.EncryptionUtils;
import org.karatachi.portus.common.http.EmbeddedPageResource;
import org.karatachi.portus.common.net.AccessInfo;
import org.karatachi.portus.common.net.AuthorizationInfo;
import org.karatachi.portus.core.entity.File;
import org.karatachi.portus.redirect.bean.FileInfo;
import org.karatachi.portus.redirect.bean.NodeInfo;
import org.karatachi.system.SystemInfo;
import org.karatachi.translator.ByteArrayTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedirectionServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final Logger accesslogger = LoggerFactory.getLogger("accesslog");

    private int[][] cellularNetwork;

    @Override
    public void init() throws ServletException {
        cellularNetwork = loadCellularNetwork();
    }

    private int[][] loadCellularNetwork() {
        ArrayList<int[]> ret = new ArrayList<int[]>();

        ConnectionManager conn = DataSourceManager.getMasterConnectionManager();
        try {
            String sql = "SELECT ip_address FROM portus.network";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                ret.add(createNetwork(rs.getString("ip_address")));
            }
        } catch (SQLException e) {
            logger.error("Cellular network address load failed.", e);
        } finally {
            conn.dispose();
        }

        return ret.toArray(new int[0][]);
    }

    private boolean isCellularNetwork(String address) {
        return inNetwork(cellularNetwork, address);
    }

    private int[] createNetwork(String address) {
        try {
            int[] network = new int[2];
            int mask = 32;

            int idx = address.indexOf("/");
            if (idx >= 0) {
                mask = Integer.parseInt(address.substring(idx + 1));
                address = address.substring(0, idx);
            }

            network[0] = toIntIpAddress(address);
            network[1] = 0xFFFFFFFF << (32 - mask);
            return network;
        } catch (NumberFormatException e) {
            return new int[] { 0, 0xFFFFFFFF };
        }
    }

    private boolean inNetwork(int[][] networks, String address) {
        int addr = toIntIpAddress(address);
        for (int[] network : networks) {
            if ((addr & network[1]) == network[0]) {
                return true;
            }
        }
        return false;
    }

    private int toIntIpAddress(String address) {
        try {
            int ret = 0;
            for (String octet : address.split("\\.")) {
                ret <<= 8;
                ret |= Integer.parseInt(octet);
            }
            return ret;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    protected void doHead(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Server",
                "Karatachi Portus 1.0-SNAPSHOT (The March Hare)");
        response.setContentLength(0);

        String host = request.getHeader("Host");
        String path = request.getRequestURI();
        Date now = new Date();

        ConnectionManager conn = DataSourceManager.getMasterConnectionManager();
        try {
            // ホストヘッダがない場合はエラー
            if (host == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            String fullPath = host.split(":")[0] + path;
            FileInfo fileInfo = getFileInfo(conn, fullPath);
            if (fileInfo == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            try {
                authentication(conn, request, fullPath, fileInfo, now);
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            response.setHeader("Content-Length",
                    Long.toString(fileInfo.getSize()));
            response.setContentType(HttpFileContentType.getContentType(fileInfo.getName()));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        } finally {
            conn.dispose();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Server",
                "Karatachi Portus 1.0-SNAPSHOT (The March Hare)");

        String host = request.getHeader("Host");
        String path = request.getRequestURI();
        Date now = new Date();

        if ("/crossdomain.xml".equals(path)) {
            responseCrossDomainXml(request, response);
            return;
        }

        ConnectionManager conn = DataSourceManager.getMasterConnectionManager();
        try {
            // ホストヘッダがない場合はエラー
            if (host == null) {
                responseErrorPage(conn, request, response, null, null,
                        HttpServletResponse.SC_NOT_FOUND, now);
                return;
            }

            String fullPath = host.split(":")[0] + path;
            FileInfo fileInfo = getFileInfo(conn, fullPath);
            if (fileInfo == null) {
                responseErrorPage(conn, request, response, null, null,
                        HttpServletResponse.SC_NOT_FOUND, now);
                return;
            } else if (fileInfo.isDirectory()) {
                responseErrorPage(conn, request, response, fileInfo, null,
                        HttpServletResponse.SC_FORBIDDEN, now);
                return;
            }

            try {
                authentication(conn, request, fullPath, fileInfo, now);
            } catch (Exception e) {
                responseErrorPage(conn, request, response, fileInfo, null,
                        HttpServletResponse.SC_FORBIDDEN, now);
                return;
            }

            NodeInfo nodeInfo = getNodeInfo(conn, fileInfo);
            AuthorizationInfo authInfo = null;

            boolean nocache = false;
            boolean nocheck = false;

            if (request.getParameter("nocheck") != null) {
                nocheck = true;
            } else if (request.getParameter("code") != null) {
                String code = request.getParameter("code");
                authInfo = AuthorizationInfo.decrypt(code);
                if (authInfo.ipAddress.equals("0.0.0.0")) {
                    nocheck = true;
                }
            }

            int available = getReplicationCount(conn, fileInfo);
            if (nodeInfo != null
                    && available >= AssemblyInfo.REPLICATION_MINIMAM) {
                switch ((int) fileInfo.getFileTypeId()) {
                case File.TYPE_FLASH_STREAMING:
                    sendRedirectToFlashMediaServer(conn, request, response,
                            fileInfo, nodeInfo, authInfo, nocache, nocheck, now);
                    break;
                default:
                    sendRedirect(conn, request, response, fileInfo, nodeInfo,
                            authInfo, nocache, nocheck, now);
                    break;
                }
            } else {
                responseErrorPage(conn, request, response, fileInfo, null,
                        HttpServletResponse.SC_CONFLICT, now);
            }
        } catch (Exception e) {
            logger.error("error", e);
            try {
                responseErrorPage(conn, request, response, null, null,
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR, now);
            } catch (SQLException ex) {
            }
            return;
        } finally {
            conn.dispose();
        }
    }

    private DateFormat LOG_DATETIME_FORMAT = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss,SSS");

    /*
     * アクセスログの書き出し
     */
    private void accessLog(ConnectionManager conn, HttpServletRequest request,
            FileInfo fileInfo, NodeInfo nodeInfo, AuthorizationInfo authInfo,
            int responseCode, Date now) throws SQLException {
        StringBuilder log = new StringBuilder();

        log.append(LOG_DATETIME_FORMAT.format(now)); //0
        log.append("\t");

        log.append(responseCode); //1
        log.append("\t");

        if (request.getQueryString() != null) {
            log.append(request.getMethod()); //2
            log.append("\t");
            log.append(request.getRequestURL()); //3
            log.append("\t");
            log.append(request.getQueryString()); //4
            log.append("\t");
            log.append(request.getProtocol()); //5
            log.append("\t");
        } else {
            log.append(request.getMethod()); //2
            log.append("\t");
            log.append(request.getRequestURL()); //3
            log.append("\t\t");
            log.append(request.getProtocol()); //5
            log.append("\t");
        }

        if (fileInfo != null) {
            log.append(fileInfo.getSize()); //6
            log.append("\t");
            log.append(fileInfo.getDomainId()); //7
            log.append("\t");
            log.append(fileInfo.getId()); //8
            log.append("\t");
        } else {
            log.append("\t\t\t");
        }

        if (nodeInfo != null) {
            log.append(nodeInfo.getId()); //9
        }
        log.append("\t");

        log.append(SystemInfo.HOST_NAME); //10
        log.append("\t");

        log.append(request.getRemoteAddr()); //11
        log.append("\t");

        if (request.getHeader("Referer") != null) {
            log.append(request.getHeader("Referer")); //12
        }
        log.append("\t");

        if (request.getHeader("User-Agent") != null) {
            log.append(request.getHeader("User-Agent")); //13
        }
        log.append("\t");

        if (authInfo != null && authInfo.username != null) {
            log.append(authInfo.username);
        }
        log.append("\t");

        accesslogger.trace(log.toString());
    }

    /*
     * ファイルの選択
     */
    private FileInfo getFileInfo(ConnectionManager conn, String fullPath)
            throws SQLException {
        String sql =
                "SELECT file.id, file.domain_id, file.directory, file.name, file.actual_authorized, file.referer, file.replication, file.size, file.file_type_id, domain.allow_from "
                        + "FROM file JOIN domain ON (file.domain_id=domain.id) WHERE full_path=? AND actual_published "
                        + "AND (actual_open_date IS NULL OR actual_open_date <= CURRENT_TIMESTAMP) "
                        + "AND (actual_close_date IS NULL OR actual_close_date > CURRENT_TIMESTAMP) "
                        + "AND domain.valid";

        PreparedStatement stmt = conn.prepareStatement(sql);
        try {
            stmt.setString(1, URLDecoder.decode(fullPath, "UTF-8"));
        } catch (UnsupportedEncodingException ignore) {
            return null;
        }

        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return new FileInfo(rs.getLong("id"), rs.getLong("domain_id"),
                    rs.getBoolean("directory"), rs.getString("name"),
                    rs.getBoolean("actual_authorized"),
                    rs.getString("referer"), rs.getInt("replication"),
                    rs.getLong("size"), rs.getLong("file_type_id"),
                    rs.getString("allow_from"), fullPath);
        } else {
            return null;
        }
    }

    /*
     * アクセス認証
     */
    private void authentication(ConnectionManager conn,
            HttpServletRequest request, String fullPath, FileInfo fileInfo,
            Date now) throws IllegalAccessException, SQLException, IOException {

        if (fileInfo.isAuthorized()) {
            if (request.getParameter("code") == null) {
                throw new IllegalAccessException("No authorization code");
            }

            String code = request.getParameter("code");
            String customerCode =
                    EncryptionUtils.customerIdToString(getCustomerId(conn,
                            fileInfo.getDomainId()));
            AuthorizationInfo authInfo = AuthorizationInfo.decrypt(code);

            if (!customerCode.equals(ByteArrayTranslator.toHex(authInfo.code))) {
                throw new IllegalAccessException("Invalide customer code");
            }

            if (!Arrays.equals(
                    authInfo.url,
                    AuthorizationInfo.digestUrl(("http://" + fullPath).toLowerCase()))) {
                throw new IllegalAccessException("Invalid url");
            }

            if (!authInfo.ipAddress.equals("0.0.0.0")
                    && !authInfo.ipAddress.equals(request.getRemoteAddr())) {
                throw new IllegalAccessException("Invalid remote address");
            }

            if (authInfo.expire != -1L && now.getTime() > authInfo.expire) {
                throw new IllegalAccessException("Invalid expire");
            }
        }

        if (fileInfo.getReferer() != null) {
            String referer = request.getHeader("Referer");
            if (referer == null || !referer.matches(fileInfo.getReferer())) {
                throw new IllegalAccessException("Invalid referer");
            }
        }

        if (fileInfo.getAllowFrom() != null) {
            ArrayList<int[]> ret = new ArrayList<int[]>();
            for (String address : fileInfo.getAllowFrom().split(",")) {
                ret.add(createNetwork(address));
            }
            if (!inNetwork(ret.toArray(new int[0][]), request.getRemoteAddr())) {
                throw new IllegalAccessException("Invalid remote address");
            }
        }
    }

    /*
     * 認証使用時のファイルのカスタマID属性の取得
     */
    private long getCustomerId(ConnectionManager conn, long domainId)
            throws SQLException {
        String sql = "SELECT customer_id FROM domain WHERE id = ?";

        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setLong(1, domainId);

        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getLong(1);
        } else {
            return -1L;
        }
    }

    /*
     * 現在の有効レプリカ情報の取得
     */
    private int getReplicationCount(ConnectionManager conn, FileInfo fileInfo)
            throws SQLException {
        String sql =
                "SELECT available AS count FROM portus.file_replication WHERE portus.file_replication.id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setLong(1, fileInfo.getId());

        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getInt(1);
        } else {
            return 0;
        }
    }

    /*
     * 転送先筐体の選択および情報の取得
     */
    private NodeInfo getNodeInfo(ConnectionManager conn, FileInfo fileInfo)
            throws Exception {
        String sql;
        switch ((int) fileInfo.getFileTypeId()) {
        case File.TYPE_FLASH_STREAMING:
            sql =
                    "SELECT portus.node.id AS node_id, ip_address, http_port, protocol_revision "
                            + "FROM portus.storedinfo JOIN portus.node ON storedinfo.node_id = portus.node.id "
                            + "WHERE portus.storedinfo.file_id = ? AND portus.node.status = 1 AND portus.node.node_type_id = 1"
                            + "ORDER BY random() LIMIT 1";
            break;
        default:
            sql =
                    "SELECT portus.node.id AS node_id, ip_address, http_port, protocol_revision "
                            + "FROM portus.storedinfo JOIN portus.node ON storedinfo.node_id = portus.node.id "
                            + "WHERE portus.storedinfo.file_id = ? AND portus.node.status = 1 "
                            + "ORDER BY random() LIMIT 1";
            break;
        }

        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setLong(1, fileInfo.getId());

        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return new NodeInfo(rs.getLong("node_id"),
                    rs.getString("ip_address"), rs.getInt("http_port"),
                    rs.getInt("protocol_revision"));
        } else {
            return null;
        }
    }

    /*
     * 成功時のリダイレクション
     */
    private void sendRedirect(ConnectionManager conn,
            HttpServletRequest request, HttpServletResponse response,
            FileInfo fileInfo, NodeInfo nodeInfo, AuthorizationInfo authInfo,
            boolean nocache, boolean nocheck, Date now) throws IOException,
            SQLException {

        // 有効期限は5分
        Date expire = new Date(now.getTime() + 5 * 60 * 1000);

        String remoteAddress;
        if (nocheck) {
            remoteAddress = "0.0.0.0";
            response.setHeader(
                    "X-Client-Type",
                    "nocheck"
                            + String.format(" (0x%08X)",
                                    toIntIpAddress(request.getRemoteAddr())));
        } else if (isCellularNetwork(request.getRemoteAddr())) {
            remoteAddress = "0.0.0.0";
            response.setHeader(
                    "X-Client-Type",
                    "cellular"
                            + String.format(" (0x%08X)",
                                    toIntIpAddress(request.getRemoteAddr())));
        } else {
            remoteAddress = request.getRemoteAddr();
            response.setHeader(
                    "X-Client-Type",
                    "general"
                            + String.format(" (0x%08X)",
                                    toIntIpAddress(request.getRemoteAddr())));
        }

        AccessInfo accessInfo =
                new AccessInfo(fileInfo.getId(), remoteAddress,
                        fileInfo.getName(), expire.getTime(), nocache, false);
        response.sendRedirect(String.format("http://%s:%s/~%s?x=%s",
                nodeInfo.getIpAddress(), nodeInfo.getPort(),
                fileInfo.getFullPath(), AccessInfo.encrypt(accessInfo)));

        accessLog(conn, request, fileInfo, nodeInfo, authInfo,
                HttpServletResponse.SC_OK, now);
    }

    private void sendRedirectToFlashMediaServer(ConnectionManager conn,
            HttpServletRequest request, HttpServletResponse response,
            FileInfo fileInfo, NodeInfo nodeInfo, AuthorizationInfo authInfo,
            boolean nocache, boolean nocheck, Date now) throws IOException,
            SQLException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getOutputStream().println(
                String.format("{ \"url\" : \"rtmp://%s/vod/%s\" }",
                        nodeInfo.getIpAddress(),
                        EncryptionUtils.toFilePathForFlash(fileInfo.getId(),
                                fileInfo.getName())));

        accessLog(conn, request, fileInfo, nodeInfo, authInfo,
                HttpServletResponse.SC_OK, now);
    }

    /*
     * エラーページ表示
     */
    private void responseErrorPage(ConnectionManager conn,
            HttpServletRequest request, HttpServletResponse response,
            FileInfo fileInfo, NodeInfo nodeInfo, int sc, Date now)
            throws IOException, SQLException {
        EmbeddedPageResource.responseErrorPage(response, sc);
        accessLog(conn, request, fileInfo, nodeInfo, null, sc, now);
    }

    /*
     * crossdomain.xml表示
     */
    private void responseCrossDomainXml(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/xml; charset=UTF-8");
        EmbeddedPageResource.response(response.getOutputStream(),
                "crossdomain.xml");
    }
}
