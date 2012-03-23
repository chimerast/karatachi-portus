package org.karatachi.portus.manage.web.daemon;

import java.util.List;

import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.karatachi.daemon.DaemonGroup;
import org.karatachi.wicket.monitor.DaemonMonitorPanel;

public class DaemonStatusPanel extends Panel {
    private static final long serialVersionUID = 1L;

    public DaemonStatusPanel(String id,
            List<Class<? extends DaemonGroup>> daemonGroups) {
        super(id);
        add(new ListView<Class<? extends DaemonGroup>>("daemonGroups",
                daemonGroups) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(
                    final ListItem<Class<? extends DaemonGroup>> item) {
                item.add(new DaemonMonitorPanel("daemon", item.getModelObject()));
            }
        });
    }
}
