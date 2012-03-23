package org.karatachi.portus.bootstrap.communicator;

import java.io.IOException;
import java.net.HttpURLConnection;

import org.karatachi.portus.bootstrap.AssemblyInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Registerer {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public boolean register() {
        return new Communicator().execute(AssemblyInfo.URL_REGISTER,
                new RegisterProcess() {
                    @Override
                    public void request(HttpURLConnection conn) {
                        conn.setRequestProperty("Node-Id",
                                Long.toString(AssemblyInfo.NODE_ID));
                        conn.setRequestProperty(
                                "Bootstrap-Revision",
                                Integer.toString(AssemblyInfo.BOOTSTRAP_REVISION));
                        conn.setRequestProperty("Status", "regist");
                    }
                });
    }

    public boolean shutdown() {
        return new Communicator().execute(AssemblyInfo.URL_REGISTER,
                new RegisterProcess() {
                    @Override
                    public void request(HttpURLConnection conn) {
                        conn.setRequestProperty("Node-Id",
                                Long.toString(AssemblyInfo.NODE_ID));
                        conn.setRequestProperty(
                                "Bootstrap-Revision",
                                Integer.toString(AssemblyInfo.BOOTSTRAP_REVISION));
                        conn.setRequestProperty("Status", "shutdown");
                    }
                });
    }

    private abstract class RegisterProcess implements Communicator.Process {
        @Override
        public boolean response(HttpURLConnection conn) throws IOException {
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return true;
            } else {
                logger.error("登録エラー: {}", conn.getResponseMessage());
                return false;
            }
        }
    }
}
