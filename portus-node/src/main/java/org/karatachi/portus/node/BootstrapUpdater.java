package org.karatachi.portus.node;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.karatachi.crypto.DigestUtils;
import org.karatachi.translator.ByteArrayTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BootstrapUpdater {
    private static final Logger logger =
            LoggerFactory.getLogger(BootstrapUpdater.class);

    public static boolean update() {
        File jarfile = null;
        try {
            logger.info("アップデート開始");

            HttpClient client = new HttpClient();

            GetMethod method = new GetMethod(AssemblyInfo.URL_UPDATE);
            method.setRequestHeader("Node-Id",
                    Long.toString(AssemblyInfo.NODE_ID));
            method.setRequestHeader("Protocol-Revision",
                    Integer.toString(AssemblyInfo.PROTOCOL_REVISION));
            method.setRequestHeader("Bootstrap-Revision",
                    Integer.toString(AssemblyInfo.BOOTSTRAP_REVISION));
            method.setRequestHeader("Node-Revision",
                    Integer.toString(AssemblyInfo.NODE_REVISION));

            int revision;
            String md5;

            OutputStream out = null;
            try {
                int status = client.executeMethod(method);
                if (status == HttpStatus.SC_NOT_MODIFIED) {
                    return false;
                } else if (status != HttpStatus.SC_OK) {
                    logger.error("アップデート失敗 : StatusCode=" + status);
                    return false;
                }

                revision =
                        Integer.parseInt(method.getResponseHeader(
                                "Bootstrap-Revision").getValue());
                md5 = method.getResponseHeader("Content-MD5").getValue();

                jarfile = File.createTempFile("portus", ".dat");

                logger.info("ダウンロード開始: Bootstrap-Revision {}", revision);
                out = new BufferedOutputStream(new FileOutputStream(jarfile));
                byte[] digest =
                        transferResource(out, method.getResponseBodyAsStream());
                if (!md5.equals(ByteArrayTranslator.toBase64(digest))) {
                    logger.error("ダウンロード: 不正なダイジェスト");
                    return false;
                }
                logger.info("ダウンロード終了: Bootstrap-Revision {}", revision);
            } finally {
                quietlyClose(out);
                method.releaseConnection();
            }

            {
                logger.info("更新ファイル検証開始: Bootstrap-Revision {}", revision);
                byte[] digest = verify(jarfile);
                if (!md5.equals(ByteArrayTranslator.toBase64(digest))) {
                    logger.error("ダウンロードファイル検証: 不正なダイジェスト");
                    return false;
                }
                logger.info("更新ファイル検証終了: Bootstrap-Revision {}", revision);
            }

            File bootstrap = new File(AssemblyInfo.PATH_BOOTSTRAP);

            {
                logger.info("上書き開始: Bootstrap-Revision {}", revision);
                byte[] digest = overwrite(bootstrap, jarfile);
                if (digest == null) {
                    logger.error("上書き: ファイルのロックに失敗");
                    return false;
                } else if (!md5.equals(ByteArrayTranslator.toBase64(digest))) {
                    logger.error("上書き: 不正なダイジェスト");
                    return false;
                }
                logger.info("上書き終了: Bootstrap-Revision {}", revision);
            }

            {
                logger.info("ブートストラップ検証開始: Bootstrap-Revision {}", revision);
                byte[] digest = verify(bootstrap);
                if (digest == null) {
                    logger.error("上書き: ファイルのロックに失敗");
                    return false;
                } else if (!md5.equals(ByteArrayTranslator.toBase64(digest))) {
                    logger.error("検証: 不正なダイジェスト");
                    return false;
                }
                logger.info("ブートストラップ検証終了: Bootstrap-Revision {}", revision);
            }

            Service.stop();
            Service.join(300000);
            Runtime.getRuntime().exec(
                    String.format(AssemblyInfo.JAVA_COMMAND,
                            AssemblyInfo.PATH_BOOTSTRAP));
            System.exit(0);

            return true;
        } catch (Exception e) {
            logger.error("アップデート失敗", e);
            return false;
        } finally {
            if (jarfile != null && !jarfile.delete()) {
                jarfile.deleteOnExit();
            }
        }
    }

    private static byte[] overwrite(File dest, File src) throws IOException {
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            in = new FileInputStream(src);
            if (in.getChannel().tryLock(0, Long.MAX_VALUE, true) == null) {
                return null;
            }

            out = new FileOutputStream(dest);
            if (out.getChannel().tryLock() == null) {
                return null;
            }

            return transferResource(out, in);
        } finally {
            quietlyClose(in);
            quietlyClose(out);
        }
    }

    private static byte[] verify(File file) throws IOException {
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            if (in.getChannel().tryLock(0, Long.MAX_VALUE, true) == null) {
                return null;
            }

            MessageDigest digest = DigestUtils.createDigest();
            int len;
            byte[] buf = new byte[8192];
            while ((len = in.read(buf)) != -1) {
                digest.update(buf, 0, len);
            }

            return digest.digest();
        } finally {
            quietlyClose(in);
        }
    }

    private static void quietlyClose(Closeable closable) {
        try {
            if (closable != null) {
                closable.close();
            }
        } catch (IOException ignore) {
        }
    }

    private static byte[] transferResource(OutputStream out, InputStream in)
            throws IOException {
        MessageDigest digest = DigestUtils.createDigest();

        int len;
        byte[] buf = new byte[8192];
        while ((len = in.read(buf)) != -1) {
            out.write(buf, 0, len);
            digest.update(buf, 0, len);
        }

        return digest.digest();
    }
}
