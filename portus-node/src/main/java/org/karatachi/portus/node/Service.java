package org.karatachi.portus.node;

public class Service {
    public static boolean debug = false;

    private static ServiceBase instance = new PortusService();

    public static boolean start() {
        return instance.startService();
    }

    public static boolean stop() {
        return instance.stopService();
    }

    public static boolean join(int timeout) {
        return instance.joinService(timeout);
    }

    public static boolean available() {
        return instance.getState() != ServiceState.TERMINATED;
    }

    public static ServiceState state() {
        return instance.getState();
    }

    public static ServiceBase getInstance() {
        return instance;
    }

    public static void main(String[] args) throws Exception {
        Service.debug = true;
        Service.start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                Service.stop();
                Service.join(0);
            }
        });
    }
}
