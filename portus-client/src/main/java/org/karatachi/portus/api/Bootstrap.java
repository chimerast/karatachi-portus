package org.karatachi.portus.api;

import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.karatachi.portus.api.type.SearchFileResponse;
import org.karatachi.portus.client.AssemblyInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bootstrap {
    private static final Logger logger =
            LoggerFactory.getLogger(Bootstrap.class);

    public static void main(String[] args) {
        try {
            testAll(args);
        } catch (Exception e) {
            logger.error("uncaught exception.", e);
            System.exit(1);
        }
    }

    public static void testAll(String[] args) throws Exception {
        clearFiles();

        String[] paths = uploadFiles(args);
        if (paths.length != Integer.parseInt(args[0])) {
            logger.error("uploadFiles() returns wrong count of files.");
            System.exit(1);
        }

        long start = System.currentTimeMillis();
        logger.info("file registered successfully.");
        while (!fileStatusCheck(paths)) {
            if ((System.currentTimeMillis() - start) > 20 * 60 * 1000) {
                logger.error("file register over 20 minutes.");
                System.exit(1);
            }
            Thread.sleep(60 * 1000);
        }
        long end = System.currentTimeMillis();
        logger.info("file distributed successfully: totaltime={} ms", end
                - start);

        String[] fullPaths = searchCheck();
        if (fullPaths.length != paths.length) {
            logger.error("searchCheck() returns wrong count of files.");
            System.exit(1);
        }
        logger.info("file searched successfully.");

        if (!downloadCheck(fullPaths)) {
            logger.error("donlowad file failed.");
            System.exit(1);
        }

        logger.info("the test success completelly.");
    }

    public static boolean clearFiles() throws Exception {
        PortusAPIConnector conn =
                new PortusAPIConnector(AssemblyInfo.TARGET_DOMAIN);
        return conn.removeFile("stress");
    }

    public static String[] uploadFiles(String[] args) {
        ArrayList<String> list = new ArrayList<String>();

        logger.info("Stress test start.");
        try {
            int count;
            try {
                count = Integer.parseInt(args[0]);
            } catch (Exception e) {
                throw new Exception("パラメータが間違っています。");
            }

            byte[] buffer = new byte[1024 * 1024];
            Random random = new Random();

            File temp = File.createTempFile("portus_stress", "dat");
            temp.deleteOnExit();

            FileRegister reg =
                    new FileRegister(AssemblyInfo.TARGET_DOMAIN,
                            AssemblyInfo.TARGET_USERID,
                            AssemblyInfo.TARGET_PASSWORD);
            for (int i = 0; i < count; ++i) {
                long start = System.currentTimeMillis();

                int len = random.nextInt(1024 * 1024);
                random.nextBytes(buffer);

                FileOutputStream out = new FileOutputStream(temp);
                out.write(buffer, 0, len);
                out.close();

                String name =
                        String.format("stress/portus_stress_%06d.dat", i + 1);
                boolean ret = reg.uploadFile(temp, name);

                long time = System.currentTimeMillis() - start;
                if (ret) {
                    logger.info(String.format(
                            "name='%s', size=%,9d bytes, time=%,9d ms", name,
                            len, time));
                    list.add(name);
                } else {
                    logger.error(String.format(
                            "name='%s', size=%,9d bytes, time=%,9d ms", name,
                            len, time));
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println("portus-client <count>");
            System.err.println("  <count> 登録するファイル数");
            System.exit(1);
        }
        return list.toArray(new String[0]);
    }

    public static boolean fileStatusCheck(String[] paths) throws Exception {
        PortusAPIConnector conn =
                new PortusAPIConnector(AssemblyInfo.TARGET_DOMAIN);

        long[] statuses = conn.getFileStatus(paths);
        logger.info("file status: {}", Arrays.toString(statuses));

        boolean ret = true;
        for (long status : statuses) {
            if (status < 2) {
                ret = false;
            }
        }
        return ret;
    }

    public static String[] searchCheck() throws Exception {
        PortusAPIConnector conn =
                new PortusAPIConnector(AssemblyInfo.TARGET_DOMAIN);

        ArrayList<String> ret = new ArrayList<String>();

        SearchFileResponse[] search = conn.search("stress/portus_stress");
        for (SearchFileResponse file : search) {
            logger.info("searched file: {}",
                    ToStringBuilder.reflectionToString(file));
            ret.add(file.getPath());
        }

        return ret.toArray(new String[0]);
    }

    public static boolean downloadCheck(String[] paths) throws Exception {
        boolean ret = true;
        for (String path : paths) {
            HttpURLConnection conn =
                    (HttpURLConnection) new URL("http://" + path).openConnection();
            try {
                conn.setRequestMethod("GET");
                conn.connect();

                logger.info("download successfully. {} responseCode={}",
                        "http://" + path, conn.getResponseCode());

                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    ret = false;
                }
            } finally {
                conn.disconnect();
            }
        }
        return ret;
    }
}
