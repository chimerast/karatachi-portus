package org.karatachi.portus.manage.web.replication;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.extensions.markup.html.tabs.TabbedPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.karatachi.daemon.DaemonGroup;
import org.karatachi.daemon.DaemonManager;
import org.karatachi.daemon.producer.ProducerWorkerDaemonGroup;
import org.karatachi.portus.manage.daemon.DirectReplicationDaemonGroup;
import org.karatachi.portus.manage.daemon.ForceReplicationDaemonGroup;
import org.karatachi.portus.manage.daemon.ReplicationDaemonGroup;
import org.karatachi.portus.manage.web.PortusBasePage;

public class ReplicationQueuePage extends PortusBasePage {
    private static final long serialVersionUID = 1L;

    public ReplicationQueuePage() {
        List<ITab> tabs = new ArrayList<ITab>();
        addTab(tabs, ReplicationDaemonGroup.class);
        addTab(tabs, DirectReplicationDaemonGroup.class);
        addTab(tabs, ForceReplicationDaemonGroup.class);
        add(new TabbedPanel("tabs", tabs));
    }

    private void addTab(List<ITab> tabs,
            final Class<? extends ProducerWorkerDaemonGroup> clazz) {
        DaemonGroup daemonGroup = DaemonManager.getDaemonGroup(clazz);
        tabs.add(new AbstractTab(new Model<String>(daemonGroup.getGroupName())) {
            private static final long serialVersionUID = 1L;

            @Override
            public Panel getPanel(String panelId) {
                return new ReplicationQueuePanel(panelId, clazz);
            }
        });
    }
}
