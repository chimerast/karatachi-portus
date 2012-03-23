package org.karatachi.portus.bootstrap.communicator;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.security.MessageDigest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.karatachi.crypto.DigestUtils;
import org.karatachi.portus.bootstrap.AssemblyInfo;
import org.karatachi.translator.ByteArrayTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Updater {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public boolean update(final int nodeRevision) {
        return new Communicator().execute(AssemblyInfo.URL_UPDATE,
                new Communicator.Process() {
                    @Override
                    public void request(HttpURLConnection conn) {
                        conn.setRequestProperty("Node-Id",
                                Long.toString(AssemblyInfo.NODE_ID));
                        conn.setRequestProperty(
                                "Bootstrap-Revision",
                                Integer.toString(AssemblyInfo.BOOTSTRAP_REVISION));
                        conn.setRequestProperty("Node-Revision",
                                Integer.toString(nodeRevision));
                    }

                    @Override
                    public boolean response(HttpURLConnection conn)
                            throws IOException {
                        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                            return false;
                        }

                        String revision = conn.getHeaderField("Node-Revision");
                        String md5 = conn.getHeaderField("Content-MD5");

                        logger.info("アップデート開始: Node-Revision {}", revision);

                        File zipfile = File.createTempFile("portus", ".dat");
                        zipfile.deleteOnExit();

                        OutputStream out =
                                new BufferedOutputStream(new FileOutputStream(
                                        zipfile));
                        try {
                            byte[] digest =
                                    transferResource(out, conn.getInputStream());
                            if (!md5.equals(ByteArrayTranslator.toBase64(digest))) {
                                logger.error("不正なダイジェスト");
                                return false;
                            }
                            out.close();

                            logger.info("ダウンロード終了: Node-Revision {}", revision);

                            unzip(zipfile, AssemblyInfo.PATH_NODE + "/"
                                    + revision);

                            logger.info("アップデート終了: Node-Revision {}", revision);

                            return true;
                        } finally {
                            out.close();
                            zipfile.delete();
                        }
                    }
                });
    }

    private void unzip(File zipfile, String rootDir) throws IOException {
        ZipInputStream in = new ZipInputStream(new FileInputStream(zipfile));
        ZipEntry entry = null;
        while ((entry = in.getNextEntry()) != null) {
            if (entry.isDirectory()) {
                new File(rootDir + "/" + entry.getName()).mkdirs();
            } else {
                new File(new File(rootDir + "/" + entry.getName()).getParent()).mkdirs();
                FileOutputStream out =
                        new FileOutputStream(rootDir + "/" + entry.getName());
                transferResource(out, in);
                out.close();
            }
            in.closeEntry();
        }
        in.close();
    }

    private byte[] transferResource(OutputStream out, InputStream in)
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
