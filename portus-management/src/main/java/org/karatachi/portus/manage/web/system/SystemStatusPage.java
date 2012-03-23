package org.karatachi.portus.manage.web.system;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.wicket.markup.html.basic.Label;
import org.karatachi.portus.manage.service.NodeManagementService;
import org.karatachi.portus.manage.web.PortusBasePage;
import org.karatachi.wicket.chart.MonitorChartImage;
import org.karatachi.wicket.label.DataSizeLabel;
import org.karatachi.wicket.label.FormattedLabel;
import org.seasar.framework.container.SingletonS2Container;
import org.seasar.framework.container.annotation.tiger.Binding;

public class SystemStatusPage extends PortusBasePage {
    private static final long serialVersionUID = 1L;

    @Binding
    private NodeManagementService nodeManagementService;

    public SystemStatusPage() {
        add(new Label("count", String.format("%,d",
                nodeManagementService.getAvailableCount())));

        add(new DataSizeLabel("totalFreespace",
                nodeManagementService.getTotalValue("FreeSpace")));
        add(new DataSizeLabel("totalSent",
                nodeManagementService.getTotalValue("Network-BytesSent")));
        add(new DataSizeLabel("totalReceived",
                nodeManagementService.getTotalValue("Network-BytesReceived")));

        add(new FormattedLabel("totalFreespaceByte", "%,d",
                nodeManagementService.getTotalValue("FreeSpace")));
        add(new FormattedLabel("totalSentByte", "%,d",
                nodeManagementService.getTotalValue("Network-BytesSent")));
        add(new FormattedLabel("totalReceivedByte", "%,d",
                nodeManagementService.getTotalValue("Network-BytesReceived")));

        add(createChart("chart1", "AvailableCount"));
        add(createChart("chart2", "Network-BytesSent", "Network-BytesReceived"));
        add(createChart("chart3", "Http-ActiveCount", "AccessPerMinute"));
    }

    private MonitorChartImage createChart(String id, String... titles) {
        return new MonitorChartImage(id, 760, 160, "system.monitor",
                "node.portus.karatachi.org", titles, 1) {
            private static final long serialVersionUID = 1L;

            @Override
            protected Connection getConnection() throws SQLException {
                return SingletonS2Container.getComponent(DataSource.class).getConnection();
            }
        };
    }
}
