package org.karatachi.portus.node.http;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PortusHttpService implements Runnable, RejectedExecutionHandler {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public static int waitingQueueSize;
    public static int minThread;
    public static int maxThread;
    public static int threadKeepAliveTime;
    public static int waitForShutdown;

    public static int soTimeout;
    public static int sendBufferSize;
    public static int recvBufferSize;

    private final Thread httpServiceThread;
    private final ServerSocket serverSocket;
    private final ThreadPoolExecutor executor;

    public PortusHttpService(int port) throws IOException {
        serverSocket = new ServerSocket();
        serverSocket.setReceiveBufferSize(recvBufferSize);
        try {
            serverSocket.bind(new InetSocketAddress(port), 200);

            BlockingQueue<Runnable> queue =
                    new LinkedBlockingQueue<Runnable>(waitingQueueSize);
            executor =
                    new ThreadPoolExecutor(minThread, maxThread,
                            threadKeepAliveTime, TimeUnit.MILLISECONDS, queue,
                            new HttpWorkerThreadFactory(), this);
            executor.prestartCoreThread();

            logger.info(
                    "MinThread: {}, MaxThread: {}, ThreadKeepAliveTime: {}",
                    new Object[] { minThread, maxThread, threadKeepAliveTime });

            // HTTPサービス受け入れスレッド開始
            httpServiceThread = new Thread(this, "HTTP");
            httpServiceThread.start();
        } catch (IOException e) {
            serverSocket.close();
            throw e;
        }
    }

    public long getQueueCount() {
        return executor.getQueue().size();
    }

    public long getActiveCount() {
        return executor.getActiveCount();
    }

    public long getTaskCount() {
        return executor.getTaskCount();
    }

    public long getCompletedTaskCount() {
        return executor.getCompletedTaskCount();
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        HttpTask task = (HttpTask) r;
        task.rejectConnection();
    }

    @Override
    public void run() {
        logger.info("[開始] LocalPort=" + serverSocket.getLocalPort());

        while (!Thread.interrupted() && !serverSocket.isClosed()) {
            try {
                Socket socket = serverSocket.accept();
                socket.setSendBufferSize(sendBufferSize);
                executor.execute(new HttpTask(socket));
            } catch (InterruptedIOException ex) {
                break;
            } catch (SocketException ignore) {
            } catch (IOException e) {
                logger.error("I/O error initialising connection thread.", e);
                break;
            }
        }

        try {
            serverSocket.close();
        } catch (IOException ignore) {
        }

        logger.info("[終了]");
    }

    public void stop() {
        try {
            serverSocket.close();
            executor.shutdown();
        } catch (IOException e) {
            logger.error("終了エラー", e);
        }
    }

    public void join() throws InterruptedException {
        httpServiceThread.join();
        executor.awaitTermination(waitForShutdown, TimeUnit.MILLISECONDS);
    }

    public int getPort() {
        return serverSocket.getLocalPort();
    }
}

class HttpWorkerThreadFactory implements ThreadFactory {
    private volatile int ThreadCounter = 1;

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, "HTTPWorker-" + (ThreadCounter++));
    }
}
