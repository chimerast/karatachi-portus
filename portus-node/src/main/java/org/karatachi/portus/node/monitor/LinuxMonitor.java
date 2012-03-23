package org.karatachi.portus.node.monitor;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.karatachi.daemon.monitor.CollectorDaemon;
import org.karatachi.translator.IntervalTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinuxMonitor extends PerformanceMonitor {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private static class Collector {
        public int index;
        public boolean diff;

        public Collector(int index, boolean diff) {
            this.index = index;
            this.diff = diff;
        }
    }

    private final Map<String, Collector> map = new HashMap<String, Collector>();

    private PerformanceCollectorDaemon daemon;

    public LinuxMonitor() {
        daemon = new PerformanceCollectorDaemon();
        daemon.startup();

        map.put("Network-BytesReceived", new Collector(0, true));
        map.put("Network-BytesSent", new Collector(1, true));
    }

    @Override
    public Set<String> getKeys() {
        return map.keySet();
    }

    @Override
    public double[] getValue(String key) {
        return getValue(map.get(key));
    }

    private double[] getValue(Collector collector) {
        double[] ret = new double[INTERVALS.length];
        for (int i = 0; i < INTERVALS.length; ++i) {
            Double value;
            if (collector.diff) {
                value =
                        daemon.getValueDiff32Bit(collector.index,
                                IntervalTranslator.sec(INTERVALS[i]));
                if (value != null) {
                    ret[i] = value / INTERVALS[i];
                }
            } else {
                value =
                        daemon.getValueAverage(collector.index,
                                IntervalTranslator.sec(INTERVALS[i]));
                if (value != null) {
                    ret[i] = value;
                }
            }
        }
        return ret;
    }

    private static class PerformanceCollectorDaemon extends CollectorDaemon {
        private static final File NET_FILE = new File("/proc/net/dev");

        private static final String[] INTERFACE_PRIORITY = { "ppp1", "ppp0",
                "eth2", "eth0", "eth1" };

        public PerformanceCollectorDaemon() {
            super("PerformanceCollector", IntervalTranslator.sec(10),
                    IntervalTranslator.min(600));
        }

        @SuppressWarnings("unchecked")
        @Override
        protected double[] collectData() {
            try {
                Map<String, double[]> ret = new HashMap<String, double[]>();

                for (String line : (List<String>) FileUtils.readLines(NET_FILE)) {
                    int idx = line.indexOf(":");
                    if (idx < 0) {
                        continue;
                    }

                    String iface = line.substring(0, idx).trim();
                    String[] data =
                            line.substring(idx + 1).trim().split("\\s+");

                    double[] trans = new double[2];
                    trans[0] = Long.parseLong(data[0]);
                    trans[1] = Long.parseLong(data[8]);
                    ret.put(iface, trans);
                }

                for (String iface : INTERFACE_PRIORITY) {
                    if (ret.containsKey(iface)) {
                        return ret.get(iface);
                    }
                }
                return null;
            } catch (IOException e) {
                return null;
            }
        }
    }
}
