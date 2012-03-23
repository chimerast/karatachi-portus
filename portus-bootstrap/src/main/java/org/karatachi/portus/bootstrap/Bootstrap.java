package org.karatachi.portus.bootstrap;

import java.io.File;
import java.util.ArrayList;

import org.karatachi.portus.bootstrap.communicator.Registerer;
import org.karatachi.portus.bootstrap.communicator.Updater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bootstrap {
    private static final Logger logger =
            LoggerFactory.getLogger(Bootstrap.class);

    public static volatile int defaultInterval = 1 * 60 * 60 * 1000;
    public static volatile int confirmInterval = 15 * 1000;
    public static volatile int waitForTerminate = 30 * 1000;

    private static volatile boolean shutdownRequested = false;

    public static void main(String[] args) {
        final Thread main = Thread.currentThread();
        main.setName("Bootstrap");

        Runtime.getRuntime().addShutdownHook(new Thread("Executioner") {
            @Override
            public void run() {
                shutdownRequested = true;
                main.interrupt();
                try {
                    main.join();
                } catch (InterruptedException e) {
                    ;
                }
            }
        });

        new File(AssemblyInfo.PATH_BOOTSTRAP).mkdirs();
        new File(AssemblyInfo.PATH_NODE).mkdirs();

        new Bootstrap().execute();
    }

    private final Registerer registerer = new Registerer();
    private final Updater updater = new Updater();

    private final ArrayList<ServiceHandler> loadedServices =
            new ArrayList<ServiceHandler>();
    private ServiceHandler currentService;

    /*
     * 監視と実行
     */
    public synchronized void execute() {
        try {
            logger.info("[[開始]]");
            while (!shutdownRequested) {
                try {
                    if (registerer.register()) {
                        logger.info("登録通知成功");
                        updateService();
                        executeService();
                        confirmInterval = defaultInterval;
                    } else {
                        logger.info("登録通知失敗");
                        confirmInterval *= 2;
                        if (confirmInterval > defaultInterval) {
                            confirmInterval = defaultInterval;
                        }
                    }

                    wait(confirmInterval);
                } catch (Exception e) {
                    if (e instanceof InterruptedException) {
                        logger.info("割り込み");
                    } else {
                        logger.error("実行エラー", e);
                    }
                }
            }
        } finally {
            shutdown();
            logger.info("[[終了]]");
        }
    }

    /*
     * 更新確認間隔の設定
     */
    public synchronized void setConfirmInterval(int interval) {
        confirmInterval = interval;
        notify();
    }

    /*
     * サービスの更新
     */
    private void updateService() {
        if (updater.update(ServiceHandler.getLatestNodeRevision())) {
            if (currentService != null) {
                if (currentService.stop()) {
                    logger.info(String.format("Portus 終了開始: Revision %s",
                            currentService.getNodeRevision()));
                } else {
                    logger.info(String.format("Portus 終了失敗: Revision %s",
                            currentService.getNodeRevision()));
                }
                currentService = null;
            }
        }
    }

    /*
     * サービスの実行
     */
    private void executeService() {
        if (currentService == null || !currentService.available()) {
            try {
                currentService = new ServiceHandler();
                if (currentService.start()) {
                    logger.info(String.format("Portus 起動開始: Revision %s",
                            currentService.getNodeRevision()));
                    loadedServices.add(currentService);
                } else {
                    logger.info(String.format("Portus 起動失敗: Revision %s",
                            currentService.getNodeRevision()));
                    currentService = null;
                }
            } catch (Exception e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                currentService = null;
            }
        }
    }

    /*
     * 終了処理
     */
    private void shutdown() {
        if (registerer.shutdown()) {
            logger.info("終了通知成功");
        } else {
            logger.info("終了通知失敗");
        }

        for (ServiceHandler service : loadedServices) {
            service.stop();
        }

        for (ServiceHandler service : loadedServices) {
            service.join(waitForTerminate);
        }
    }
}
