package org.karatachi.portus.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ServiceBase implements Runnable {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final Thread thread = new Thread(this, "System");
    private volatile boolean shutdownRequested = false;

    protected abstract void initialize();

    protected abstract void loop() throws InterruptedException;

    protected abstract void uninitialize();

    public final void run() {
        logger.info("[全開始]");
        initialize();

        while (!shutdownRequested) {
            try {
                loop();
            } catch (InterruptedException e) {
                logger.info("割り込み");
            }
        }

        uninitialize();
        logger.info("[全終了]");
    }

    public synchronized final boolean startService() {
        if (thread.getState() == Thread.State.NEW) {
            logger.info("[開始要求]");
            thread.start();
            return true;
        } else {
            return false;
        }
    }

    public synchronized final boolean stopService() {
        if (thread.getState() != Thread.State.NEW && !shutdownRequested) {
            logger.info("[終了要求]");
            shutdownRequested = true;
            thread.interrupt();
            return true;
        } else {
            return false;
        }
    }

    public synchronized final boolean joinService(int timeout) {
        if (shutdownRequested) {
            logger.info("[待機要求]");
            try {
                thread.join(timeout);
            } catch (InterruptedException e) {
            }
            return thread.getState() == Thread.State.TERMINATED;
        } else {
            return false;
        }
    }

    public final ServiceState getState() {
        switch (thread.getState()) {
        case NEW:
            return ServiceState.NEW;
        case TERMINATED:
            return ServiceState.TERMINATED;
        default:
            if (shutdownRequested)
                return ServiceState.TERMINATING;
            else
                return ServiceState.RUNNING;
        }
    }
}
