package org.karatachi.portus.admin.web.manage;

import org.karatachi.portus.admin.web.PortusBasePage;
import org.karatachi.portus.core.auth.Authorize;
import org.karatachi.portus.core.type.AccountRole;
import org.karatachi.wicket.monitor.DataSourceMonitorPanel;
import org.karatachi.wicket.monitor.MemoryMonitorPanel;

@Authorize(AccountRole.Bit.DEVEL)
public class MemoryViewPage extends PortusBasePage {
    private static final long serialVersionUID = 1L;

    public MemoryViewPage() {
        add(new MemoryMonitorPanel("memory"));
        add(new DataSourceMonitorPanel("datasource"));
    }

    @Override
    protected String getPageTitle() {
        return "メモリ監視";
    }
}
