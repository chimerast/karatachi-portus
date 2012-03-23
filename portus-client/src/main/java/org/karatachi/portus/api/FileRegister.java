package org.karatachi.portus.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Random;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.karatachi.portus.client.AssemblyInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileRegister {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private static final DateFormat FTPNAME_FORMAT = new SimpleDateFormat(
            "yyMMddHHmmssSSS");
    private static final Random FTPNAME_RAND = new Random();

    private final PortusAPIConnector conn;
    private final String username;
    private final String password;

    public FileRegister(String domain, String username, String password) {
        this.conn = new PortusAPIConnector(domain);
        this.username = username;
        this.password = password;
    }

    public void uploadDir(File dir, String base) {
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                uploadFile(file, base + "/" + file.getName());
            } else if (file.isDirectory()) {
                uploadDir(file, base + "/" + file.getName());
            }
        }
    }

    public boolean uploadFile(File file, String name) {
        String ftpname =
                FTPNAME_FORMAT.format(System.currentTimeMillis()) + "_"
                        + FTPNAME_RAND.nextInt(10000);
        try {
            if (!file.exists() || !file.isFile()) {
                logger.error("file not found.");
                return false;
            }

            logger.debug("file registering {} to {}", file.getAbsolutePath(),
                    name);

            if (storeFile(file, ftpname)) {
                logger.info("file stored '{}'.", ftpname);
            } else {
                logger.error("file storing failed '{}'.", ftpname);
                return false;
            }

            {
                long start = System.currentTimeMillis();
                if (conn.registerFile(name, ftpname)) {
                    logger.info("file registered '{}'.", name);
                } else {
                    logger.error("file registering failed '{}'.", name);
                    return false;
                }
                logger.debug("http register time : {}ms",
                        System.currentTimeMillis() - start);
            }

            return true;
        } catch (IOException e) {
            logger.error("io exception.", e);
            return false;
        }
    }

    private boolean storeFile(File file, String name) throws IOException {
        FTPClient ftp = new FTPClient();
        try {
            ftp.connect(AssemblyInfo.FTP_HOSTNAME);

            int reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                throw new IOException("ftp negotiation failed.");
            }

            {
                logger.debug("connected to {}.", AssemblyInfo.FTP_HOSTNAME);
                long start = System.currentTimeMillis();
                if (!ftp.login(username, password)) {
                    throw new IOException("ftp login failed.");
                }
                logger.trace("ftp login time : {}ms",
                        System.currentTimeMillis() - start);

                logger.debug("logined to {}.", AssemblyInfo.FTP_HOSTNAME);
            }

            ftp.setFileType(FTP.BINARY_FILE_TYPE);

            boolean ret;
            {
                logger.debug("file storing '{}'...", file.getAbsolutePath());
                long start = System.currentTimeMillis();
                ret = ftp.storeFile(name, new FileInputStream(file));
                logger.trace("ftp store time : {}ms",
                        System.currentTimeMillis() - start);
            }
            return ret;
        } finally {
            ftp.disconnect();
        }
    }
}
