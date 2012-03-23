package org.karatachi.portus.node;

import java.io.File;
import java.io.IOException;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.karatachi.portus.node.http.PortusHttpService;
import org.karatachi.portus.node.monitor.PerformanceMonitor;
import org.karatachi.portus.node.rsh.PortusRshService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PortusService extends ServiceBase {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private PortusRshService rshService;
    private PortusHttpService httpService;

    private PerformanceMonitor performanceMonitor;

    private boolean successHostUpdate;
    private boolean serviceStarted;
    private String hostname;
    private long prevPortOpenTime;

    public PortusHttpService getHttpService() {
        return httpService;
    }

    public PerformanceMonitor getPerformanceMonitor() {
        return performanceMonitor;
    }

    @Override
    protected void initialize() {
        new File(AssemblyInfo.PATH_LOG).mkdirs();

        if (!Service.debug) {
            logger.info("System-Revision: {}", AssemblyInfo.NODE_REVISION);
            logger.info("Bootstrap-Revision: {}",
                    AssemblyInfo.BOOTSTRAP_REVISION);

            if (AssemblyInfo.BOOTSTRAP_REVISION < AssemblyInfo.REQUIRED_BOOTSTRAP_REVESION) {
                for (int i = 0; i < 5; ++i) {
                    if (BootstrapUpdater.update()) {
                        return;
                    }

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }

    private boolean startServices() {
        if (rshService == null && AssemblyInfo.RSH_PORT != 0) {
            try {
                rshService = new PortusRshService(AssemblyInfo.RSH_PORT);
            } catch (Exception e) {
                logger.error("暗号化コントロール初期化失敗 : " + e.getMessage());
                return false;
            }
        }

        if (httpService == null && AssemblyInfo.HTTP_PORT != 0) {
            try {
                httpService = new PortusHttpService(AssemblyInfo.HTTP_PORT);
            } catch (Exception e) {
                logger.error("HTTP初期化失敗 : " + e.getMessage());
                return false;
            }
        }

        if (performanceMonitor == null) {
            performanceMonitor = PerformanceMonitor.createInstance();
        }

        return updateChassisStatus("Initial");
    }

    private boolean updateChassisStatus(String status) {
        HttpClient client = new HttpClient();

        GetMethod method = new GetMethod(AssemblyInfo.URL_REGISTER);
        method.setRequestHeader("Node-Id", Long.toString(AssemblyInfo.NODE_ID));
        method.setRequestHeader("Ctrl-Port",
                Integer.toString(AssemblyInfo.RSH_PORT));
        method.setRequestHeader("Http-Port",
                Integer.toString(AssemblyInfo.HTTP_PORT));
        method.setRequestHeader("Protocol-Revision",
                Integer.toString(AssemblyInfo.PROTOCOL_REVISION));
        method.setRequestHeader("Bootstrap-Revision",
                Integer.toString(AssemblyInfo.BOOTSTRAP_REVISION));
        method.setRequestHeader("Node-Revision",
                Integer.toString(AssemblyInfo.NODE_REVISION));
        method.setRequestHeader("Status", status);

        try {
            int sc = client.executeMethod(method);
            if (sc == HttpStatus.SC_OK) {
                Header header = method.getResponseHeader("Accessed-Host");
                if (header != null) {
                    hostname = header.getValue();
                }
                logger.info("情報登録成功 : " + hostname);
                return true;
            } else {
                logger.error("情報登録失敗 : StatusCode=" + sc);
                return false;
            }
        } catch (IOException e) {
            logger.error("情報登録失敗", e);
            return false;
        } finally {
            method.releaseConnection();
        }
    }

    @Override
    protected void loop() throws InterruptedException {
        if (!successHostUpdate) {
            int interval = 30 * 1000;
            if (System.currentTimeMillis() > prevPortOpenTime + interval) {
                if (!serviceStarted) {
                    serviceStarted = startServices();
                }

                if (serviceStarted && !successHostUpdate) {
                    successHostUpdate = updateChassisStatus("HostUpdate");
                }

                prevPortOpenTime = System.currentTimeMillis();
            }
        }
        Thread.sleep(1000);
    }

    @Override
    protected void uninitialize() {
        updateChassisStatus("Final");

        if (httpService != null) {
            httpService.stop();
        }

        if (rshService != null) {
            rshService.stop();
        }

        try {
            if (httpService != null) {
                httpService.join();
            }
        } catch (Exception e) {
            logger.warn("HttpService join エラー", e);
        }

        try {
            if (rshService != null) {
                rshService.join();
            }
        } catch (Exception e) {
            logger.warn("SecuredRshService join エラー", e);
        }
    }
}
