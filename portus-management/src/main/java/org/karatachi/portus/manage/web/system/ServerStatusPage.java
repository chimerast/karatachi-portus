package org.karatachi.portus.manage.web.system;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.karatachi.portus.manage.web.PortusBasePage;
import org.karatachi.system.SystemInfo;
import org.karatachi.wicket.chart.MonitorChartImage;
import org.karatachi.wicket.monitor.DataSourceMonitorPanel;
import org.karatachi.wicket.monitor.MemoryMonitorPanel;
import org.seasar.framework.container.SingletonS2Container;

public class ServerStatusPage extends PortusBasePage {
    private static final long serialVersionUID = 1L;

    public ServerStatusPage() {
        add(new DataSourceMonitorPanel("datasource"));
        add(new MemoryMonitorPanel("memory"));

        add(createChart("chart1", "HeapMemoryUsage.init",
                "HeapMemoryUsage.used", "HeapMemoryUsage.committed",
                "HeapMemoryUsage.max"));
        add(createChart("chart2", "NonHeapMemoryUsage.init",
                "NonHeapMemoryUsage.used", "NonHeapMemoryUsage.committed",
                "NonHeapMemoryUsage.max"));
    }

    private MonitorChartImage createChart(String id, String... titles) {
        return new MonitorChartImage(id, 760, 160, "system.monitor",
                SystemInfo.HOST_NAME, titles, 1) {
            private static final long serialVersionUID = 1L;

            @Override
            protected Connection getConnection() throws SQLException {
                return SingletonS2Container.getComponent(DataSource.class).getConnection();
            }
        };
    }
}
