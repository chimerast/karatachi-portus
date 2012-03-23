package org.karatachi.portus.manage.daemon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.management.JMException;
import javax.sql.DataSource;

import org.karatachi.daemon.monitor.MBeanMonitorDaemon;
import org.karatachi.daemon.monitor.MonitorDaemon;
import org.karatachi.portus.manage.dto.NodeConditionDto;
import org.karatachi.portus.manage.service.NodeManagementService;
import org.karatachi.translator.IntervalTranslator;
import org.seasar.framework.container.SingletonS2Container;
import org.seasar.framework.container.annotation.tiger.Binding;

public class MonitorDaemonGroup {
    public static class PortusMonitorDaemon extends MonitorDaemon {
        public static final long[] INTERVAL = new long[] {
                IntervalTranslator.sec(15), IntervalTranslator.min(1),
                IntervalTranslator.min(5), IntervalTranslator.hour(1),
                IntervalTranslator.day(1) };
        public static final long[] EXPIRE = new long[] {
                IntervalTranslator.hour(5), IntervalTranslator.day(1),
                IntervalTranslator.day(7), IntervalTranslator.day(30),
                IntervalTranslator.day(365 * 10) };

        @Binding
        private NodeManagementService nodeManagementService;

        public PortusMonitorDaemon() {
            super("PortusMonitor", "system.monitor",
                    "node.portus.karatachi.org");
        }

        @Override
        protected long[] getInterval() {
            return INTERVAL;
        }

        @Override
        protected long[] getExpire() {
            return EXPIRE;
        }

        @Override
        protected Connection getConnection() throws SQLException {
            return SingletonS2Container.getComponent(DataSource.class).getConnection();
        }

        @Override
        protected void collectData() throws SQLException {
            storeValue(System.currentTimeMillis(), "AvailableCount",
                    nodeManagementService.getAvailableCount());
            collect("FreeSpace");
            collect("Network-BytesSent");
            collect("Network-BytesReceived");
            collect("Http-ActiveCount");
            collectNode();
        }

        private void collect(String key) throws SQLException {
            storeValue(key, nodeManagementService.getTotalValue(key));
        }

        private void collectNode() throws SQLException {
            List<NodeConditionDto> nodes =
                    nodeManagementService.getNodeList(new NodeConditionDto.IdComparator(
                            true));
            for (NodeConditionDto node : nodes) {
                try {
                    new NodeMonitorDaemon(node).exec();
                } catch (Exception e) {
                }
            }
        }

        public class NodeMonitorDaemon extends MonitorDaemon {
            public final long[] INTERVAL = new long[] {
                    IntervalTranslator.min(1), IntervalTranslator.min(5),
                    IntervalTranslator.hour(1), IntervalTranslator.day(1) };
            public final long[] EXPIRE = new long[] {
                    IntervalTranslator.hour(4), IntervalTranslator.day(1),
                    IntervalTranslator.day(7), IntervalTranslator.day(365) };

            private NodeConditionDto node;

            public NodeMonitorDaemon(NodeConditionDto node) {
                super("PortusMonitor", "system.monitor", String.format(
                        "%012x.node.portus.karatachi.org", node.getNode().id));
                this.node = node;
            }

            public void exec() throws Exception {
                work();
            }

            @Override
            protected long[] getInterval() {
                return INTERVAL;
            }

            @Override
            protected long[] getExpire() {
                return EXPIRE;
            }

            @Override
            protected Connection getConnection() throws SQLException {
                return PortusMonitorDaemon.this.getConnection();
            }

            @Override
            protected void collectData() throws SQLException {
                storeValue("Status", node.getStatus());
                collect("FreeSpace");
                collect("Network-BytesSent");
                collect("Network-BytesReceived");
                collect("Http-ActiveCount");
            }

            private void collect(String key) throws SQLException {
                storeValue(key, node.getPerformance().get(key));
            }
        }
    }

    public static class ServerMonitorDaemon extends MBeanMonitorDaemon {
        public ServerMonitorDaemon() {
            super("ServerMonitor", "system.monitor");

            try {
                BufferedReader in =
                        new BufferedReader(
                                new InputStreamReader(
                                        getClass().getResourceAsStream(
                                                "monitor.mbean")));
                String line;
                while ((line = in.readLine()) != null) {
                    try {
                        addAccessor(line.substring(line.lastIndexOf(":") + 1),
                                line);
                    } catch (JMException e) {
                    }
                }
            } catch (IOException ignore) {
            }
        }

        @Override
        protected Connection getConnection() throws SQLException {
            return SingletonS2Container.getComponent(DataSource.class).getConnection();
        }
    }
}
