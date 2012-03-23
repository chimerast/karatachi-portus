package org.karatachi.portus.manage.web.daemon;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.extensions.markup.html.tabs.TabbedPanel;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.karatachi.daemon.DaemonGroup;
import org.karatachi.daemon.DaemonManager;
import org.karatachi.portus.manage.daemon.DirectReplicationDaemonGroup;
import org.karatachi.portus.manage.daemon.ForceReplicationDaemonGroup;
import org.karatachi.portus.manage.daemon.NodeCheckDaemonGroup;
import org.karatachi.portus.manage.daemon.NodeMaintenanceDaemonGroup;
import org.karatachi.portus.manage.daemon.ReplicationDaemonGroup;
import org.karatachi.portus.manage.web.PortusBasePage;

public class DaemonStatusPage extends PortusBasePage {
    private static final long serialVersionUID = 1L;

    public DaemonStatusPage() {
        add(new Link<String>("startup") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                startupDaemon(NodeCheckDaemonGroup.class, 10);
                startupDaemon(NodeMaintenanceDaemonGroup.class, 5);

                startupDaemon(ReplicationDaemonGroup.class, 20);
                startupDaemon(DirectReplicationDaemonGroup.class, 10);
                startupDaemon(ForceReplicationDaemonGroup.class, 4);
            };
        });
        add(new Link<String>("shutdown") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                DaemonManager.shutdownAll();
            };
        });

        List<ITab> tabs = new ArrayList<ITab>();
        {
            ArrayList<Class<? extends DaemonGroup>> groups =
                    new ArrayList<Class<? extends DaemonGroup>>();
            groups.add(NodeCheckDaemonGroup.class);
            groups.add(NodeMaintenanceDaemonGroup.class);
            addTab(tabs, "筐体メンテナンス", groups);
        }
        {
            ArrayList<Class<? extends DaemonGroup>> groups =
                    new ArrayList<Class<? extends DaemonGroup>>();
            groups.add(ReplicationDaemonGroup.class);
            groups.add(DirectReplicationDaemonGroup.class);
            groups.add(ForceReplicationDaemonGroup.class);
            addTab(tabs, "分散管理", groups);
        }
        {
            ArrayList<Class<? extends DaemonGroup>> groups =
                    new ArrayList<Class<? extends DaemonGroup>>();
            groups.add(DaemonManager.UniqueDaemonGroup.class);
            addTab(tabs, "システム監視", groups);
        }
        add(new TabbedPanel("tabs", tabs));
    }

    private void addTab(List<ITab> tabs, String name,
            final List<Class<? extends DaemonGroup>> daemonGroups) {
        tabs.add(new AbstractTab(new Model<String>(name)) {
            private static final long serialVersionUID = 1L;

            @Override
            public Panel getPanel(String panelId) {
                return new DaemonStatusPanel(panelId, daemonGroups);
            }
        });
    }

    private void startupDaemon(Class<? extends DaemonGroup> daemonGroup,
            int count) {
        DaemonGroup group = DaemonManager.getDaemonGroup(daemonGroup);
        if (group != null) {
            group.startup();
            group.setCount(count);
        }
    }
}
