package org.karatachi.portus.node.monitor;

import java.util.Set;

public abstract class PerformanceMonitor {
    protected static final int[] INTERVALS = new int[] { 1, 5, 15, 60, 300 };

    public abstract Set<String> getKeys();

    public abstract double[] getValue(String key);

    public static PerformanceMonitor createInstance() {
        String os = System.getProperty("os.name");
        if (os == null) {
            throw new IllegalStateException("system property 'os.name' is null");
        } else if (os.startsWith("Windows")) {
            return new WindowsMonitor();
        } else if (os.startsWith("Linux")) {
            return new LinuxMonitor();
        } else {
            throw new IllegalStateException("Unknown operating system");
        }
    }
}
