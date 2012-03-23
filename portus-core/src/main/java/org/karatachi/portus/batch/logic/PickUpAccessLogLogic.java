package org.karatachi.portus.batch.logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang.StringUtils;
import org.karatachi.portus.batch.ProcessLogic;
import org.karatachi.portus.core.AssemblyInfo;

public class PickUpAccessLogLogic extends ProcessLogic {
    private DateFormat LOG_DATETIME_FORMAT = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss,SSS");
    private DateFormat LOG_APACHE_FORMAT = new SimpleDateFormat(
            "dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH);

    @Override
    public void run() {
    }

    public void run(Writer out, long domainId, int year, int month, int day)
            throws IOException {
        for (int i = 0; i < 24; ++i) {
            Map<Long, String> temp = new TreeMap<Long, String>();
            for (File host : new File(AssemblyInfo.PATH_ACCESS_LOG).listFiles()) {
                File dir =
                        new File(host, String.format("%04d/%02d/%02d", year,
                                month, day));
                File file =
                        new File(dir, String.format(
                                "accesslog_%02d-%02d-%02d_%02d.log.gz", year,
                                month, day, i));
                if (file.exists()) {
                    pickup(file, temp, domainId);
                }
            }
            for (String line : temp.values()) {
                out.write(line);
            }
        }
        out.flush();
    }

    private void pickup(File file, Map<Long, String> out, long domainId) {
        try {
            BufferedReader in =
                    new BufferedReader(new InputStreamReader(
                            new GZIPInputStream(new FileInputStream(file))));

            int count = 1;
            String line = null;
            while ((line = in.readLine()) != null) {
                try {
                    String[] data = line.split("\t");
                    if (StringUtils.isEmpty(data[7])
                            || Long.parseLong(data[7]) != domainId) {
                        continue;
                    }

                    Date date = LOG_DATETIME_FORMAT.parse(data[0]);

                    StringBuilder log = new StringBuilder();
                    log.append(data[11]);
                    log.append(" - - [");
                    log.append(LOG_APACHE_FORMAT.format(date));
                    log.append("] \"");
                    log.append(data[2]);
                    log.append(" ");
                    log.append(data[3].substring(data[3].indexOf('/', 7)));
                    log.append(" ");
                    log.append(data[5]);
                    log.append("\" ");
                    log.append(data[1]);
                    log.append(" ");
                    log.append(data[6]);
                    log.append(" \"");
                    log.append(getData(data, 12));
                    log.append("\" \"");
                    log.append(getData(data, 13));
                    log.append("\"\n");

                    out.put(date.getTime(), log.toString());
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

    private String getData(String[] data, int index) {
        if (index < data.length && !StringUtils.isEmpty(data[index])) {
            return data[index];
        } else {
            return "-";
        }
    }
}
