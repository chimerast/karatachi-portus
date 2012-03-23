package org.karatachi.portus.bootstrap.communicator;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Communicator {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public static int connectTimeout = 5000;
    public static int readTimeout = 20000;

    public interface Process {
        void request(HttpURLConnection conn);

        boolean response(HttpURLConnection conn) throws IOException;
    }

    public boolean execute(String url, Process process) {
        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
        } catch (IOException e) {
            logger.error("コネクション作成失敗", e);
            return false;
        }

        connection.setConnectTimeout(connectTimeout);
        connection.setReadTimeout(readTimeout);

        process.request(connection);

        try {
            try {
                connection.connect();
            } catch (IOException e) {
                logger.error("接続失敗", e);
                return false;
            }

            try {
                return process.response(connection);
            } catch (IOException e) {
                logger.error("データ受信失敗", e);
                return false;
            }
        } finally {
            connection.disconnect();
        }
    }
}
