package org.karatachi.portus.manage.web.system;

import java.io.File;

import org.karatachi.portus.manage.web.PortusBasePage;
import org.karatachi.wicket.monitor.LogMonitorPanel;

public class SystemLogViewPage extends PortusBasePage {
    private static final long serialVersionUID = 1L;

    public SystemLogViewPage() {
        File[] logs =
                new File[] { new File("/portus/log/manage/error.log"),
                        new File("/portus/log/admin/error.log"),
                        new File("/portus/log/dist/error.log") };
        add(new LogMonitorPanel("tabs", logs) {
            private static final long serialVersionUID = 1L;

            @Override
            public String getTabName(String filename) {
                if (filename.contains("manage")) {
                    return "管理・監視";
                } else if (filename.contains("admin")) {
                    return "ファイル管理";
                } else if (filename.contains("dist")) {
                    return "分散";
                } else {
                    return "Unknown";
                }
            }
        });
    }
}
