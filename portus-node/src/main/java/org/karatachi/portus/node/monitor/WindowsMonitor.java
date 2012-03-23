package org.karatachi.portus.node.monitor;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.karatachi.jni.win32.PerformanceCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WindowsMonitor extends PerformanceMonitor {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<String, String> map = new HashMap<String, String>();

    public WindowsMonitor() {
        PerformanceCounter.initialize(INTERVALS);

        Properties props = new Properties();
        try {
            props.load(getClass().getResourceAsStream(
                    "/performance-counter.properties"));

            Enumeration<?> names = props.propertyNames();
            while (names.hasMoreElements()) {
                String name = (String) names.nextElement();
                String path = props.getProperty(name);
                if (PerformanceCounter.addCounter(path)) {
                    map.put(name, path);
                }
            }
        } catch (IOException e) {
            logger.error("プロパティファイル読み込み失敗", e);
        }
    }

    @Override
    public Set<String> getKeys() {
        return map.keySet();
    }

    @Override
    public double[] getValue(String key) {
        return PerformanceCounter.getCounterValue(map.get(key));
    }
}
