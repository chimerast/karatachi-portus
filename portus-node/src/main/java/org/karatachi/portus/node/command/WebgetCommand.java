package org.karatachi.portus.node.command;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.Date;

import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.karatachi.crypto.DigestUtils;
import org.karatachi.net.shell.Command;
import org.karatachi.portus.common.crypto.EncryptionUtils;
import org.karatachi.portus.common.net.AccessInfo;
import org.karatachi.portus.node.AbstractCommand;
import org.karatachi.portus.node.AssemblyInfo;
import org.karatachi.translator.ByteArrayTranslator;

public class WebgetCommand extends AbstractCommand {
    public static final long NOTIFY_INTERVAL = 3 * 1000;
    public static final int HTTP_SOCKET_TIMEOUT = 30 * 1000;

    @Override
    protected int command(String[] args) throws IOException {
        if (args.length < 4) {
            return Command.INVALID_ARGUMENT_COUNTS;
        }

        String host = args[1];
        int port = 0;
        long fileId = 0;

        try {
            port = Integer.parseInt(args[2]);
            fileId = Long.parseLong(args[3]);
        } catch (Exception e) {
            return Command.INVALID_ARGUMENT;
        }

        String fileName = "";
        if (args.length >= 5) {
            fileName = args[4];
        }

        Date expire = new Date(new Date().getTime() + 5 * 60 * 1000);

        AccessInfo info =
                new AccessInfo(fileId, "0.0.0.0", fileName, expire.getTime(),
                        false, false);

        GetMethod method = new GetMethod("/?x=" + AccessInfo.encrypt(info));

        HttpConnection conn = new HttpConnection(host, port);
        conn.getParams().setConnectionTimeout(HTTP_SOCKET_TIMEOUT);
        conn.getParams().setSoTimeout(HTTP_SOCKET_TIMEOUT);

        File file = null;
        FileOutputStream fostrm = null;
        try {
            conn.open();

            HttpState state = new HttpState();
            method.execute(state, conn);

            File dir = new File(AssemblyInfo.PATH_RAW_DATA);
            if (!dir.exists() && !dir.mkdirs()) {
                sendStatus(300, "Could Not Make Directory");
                return 1;
            }

            // 古いファイルの除去
            file = new File(dir, EncryptionUtils.toFilePath(info.fileId));
            if (file.exists()) {
                if (!file.delete()) {
                    sendStatus(300, "Could Not Delete Existing File");
                    return 1;
                }
            }

            file =
                    new File(dir, EncryptionUtils.toFilePathWithExtension(
                            info.fileId, fileName));
            if (file.exists()) {
                if (!file.delete()) {
                    sendStatus(300, "Could Not Delete Existing File");
                    return 1;
                }
            }

            file.getParentFile().mkdirs();

            fostrm = new FileOutputStream(file);

            long size = method.getResponseContentLength();

            if (method.getStatusLine() != null
                    && method.getStatusCode() == HttpStatus.SC_OK) {

                InputStream in = method.getResponseBodyAsStream();
                OutputStream out = fostrm;
                MessageDigest digest = DigestUtils.createDigest();

                long prevTime = System.currentTimeMillis();
                long prevCount = 0;
                long count = 0;

                byte[] buf = new byte[8192];
                int len = 0;
                while ((len = in.read(buf)) != -1) {
                    out.write(buf, 0, len);
                    digest.update(buf, 0, len);

                    count += len;
                    if (System.currentTimeMillis() - prevTime > NOTIFY_INTERVAL) {
                        long time = System.currentTimeMillis();
                        String message =
                                String.format("Notify %s %s %s%% (%s bytes/s)",
                                        count, size, 100 * count / size,
                                        (count - prevCount) * 1000
                                                / (time - prevTime));
                        sendTemporaryStatus(300, message);
                        prevTime = time;
                        prevCount = count;
                    }
                }
                out.flush();

                String digestHex =
                        ByteArrayTranslator.toBase64(digest.digest());
                sendStatus(200, "OK");
                sendHeader("Content-Length", count);
                sendHeader("Content-MD5", digestHex);
            } else {
                try {
                    if (fostrm != null) {
                        fostrm.close();
                    }
                    if (file != null) {
                        file.delete();
                    }
                } catch (IOException e2) {
                    logger.warn("ファイル削除失敗", e2);
                }
                sendStatus(400, method.getStatusText());
            }
        } catch (Exception e) {
            logger.warn("ダウンロード失敗", e);
            try {
                if (fostrm != null) {
                    fostrm.close();
                }
                if (file != null) {
                    file.delete();
                }
            } catch (IOException e2) {
                logger.warn("ファイル削除失敗", e2);
            }
            sendStatus(400, e.getMessage());
        } finally {
            conn.close();
            if (fostrm != null) {
                fostrm.close();
            }
        }
        return Command.OK;
    }

    @Override
    public String getCommand() {
        return "wget";
    }
}
