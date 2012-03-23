package org.karatachi.portus.batch.logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.karatachi.portus.batch.ProcessLogic;
import org.karatachi.portus.core.AssemblyInfo;
import org.karatachi.portus.core.dao.AccessCountDao;
import org.seasar.framework.container.annotation.tiger.Binding;

public class AggregateAccessLogLogic extends ProcessLogic {
    public static class Aggregated {
        private int code;
        private String method;
        private String fullPath;
        private long domainId;
        private long count;
        private long transfer;
    }

    @Binding
    private AccessCountDao accessCountDao;

    private int year;
    private int month;
    private int day;
    private int hour;

    @Override
    public void run() {
        try {
            parseArgs();
        } catch (Exception e) {
            logger.error("不正な引数です。", e);
            return;
        }

        List<File> files = new ArrayList<File>();
        for (File host : new File(AssemblyInfo.PATH_ACCESS_LOG).listFiles()) {
            File dir =
                    new File(host, String.format("%04d/%02d/%02d", year, month,
                            day));
            File file =
                    new File(dir, String.format(
                            "accesslog_%02d-%02d-%02d_%02d.log.gz", year,
                            month, day, hour));
            if (file.exists()) {
                files.add(file);
            }
        }

        Date date =
                new GregorianCalendar(year, month - 1, day, hour, 0).getTime();
        for (Aggregated a : aggregate(files).values()) {
            accessCountDao.delete(date, a.code, a.method, a.fullPath);
            accessCountDao.insert(date, a.code, a.method, a.fullPath,
                    a.domainId, a.count, a.transfer);
        }
    }

    private void parseArgs() {
        year = Integer.parseInt(args[0]);
        month = Integer.parseInt(args[1]);
        day = Integer.parseInt(args[2]);
        hour = Integer.parseInt(args[3]);
    }

    private Map<String, Aggregated> aggregate(List<File> files) {
        Map<String, Aggregated> ret = new HashMap<String, Aggregated>();

        for (File file : files) {
            try {
                BufferedReader in =
                        new BufferedReader(new InputStreamReader(
                                new GZIPInputStream(new FileInputStream(file))));

                int count = 1;
                String line = null;
                while ((line = in.readLine()) != null) {
                    try {
                        String[] data = line.split("\t");

                        String responseCode = data[1];
                        String method = data[2];
                        String fullPath =
                                data[3].substring(data[3].indexOf("://") + 3);

                        String key =
                                responseCode + ":" + method + ":" + fullPath;
                        Aggregated aggregated = ret.get(key);
                        if (aggregated == null) {
                            aggregated = new Aggregated();
                            aggregated.fullPath = fullPath;
                            aggregated.code = Integer.parseInt(responseCode);
                            aggregated.method = method;
                            ret.put(key, aggregated);
                        }

                        if (aggregated.domainId == 0 && !data[7].equals("")) {
                            aggregated.domainId = Long.parseLong(data[7]);
                        }

                        ++aggregated.count;

                        if (responseCode.equals("200")) {
                            aggregated.transfer += Long.parseLong(data[6]);
                        }
                    } catch (Exception e) {
                        logger.error("集計失敗: " + file.getAbsolutePath() + ":"
                                + count, e);
                    }
                    ++count;
                }
            } catch (IOException e) {
                logger.error("集計失敗: " + file.getAbsolutePath(), e);
            }
        }
        return ret;
    }
}
