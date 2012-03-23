package org.karatachi.portus.manage.web.replication;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.karatachi.daemon.DaemonManager;
import org.karatachi.daemon.producer.ProducerWorkerDaemonGroup;
import org.karatachi.daemon.producer.WorkerTask;
import org.karatachi.wicket.auto.AutoResolvePageableListView;
import org.karatachi.wicket.listview.SimplePageableListViewNavigator;

public class ReplicationQueuePanel extends Panel {
    private static final long serialVersionUID = 1L;

    public ReplicationQueuePanel(String id,
            final Class<? extends ProducerWorkerDaemonGroup> clazz) {
        super(id, new LoadableDetachableModel<List<WorkerTask>>() {
            private static final long serialVersionUID = 1L;

            @Override
            protected List<WorkerTask> load() {
                ProducerWorkerDaemonGroup daemonGroup =
                        DaemonManager.getDaemonGroup(clazz);
                return new ArrayList<WorkerTask>(daemonGroup.getTaskQueue());
            }
        });

        AutoResolvePageableListView<WorkerTask> table;
        add(table =
                new AutoResolvePageableListView<WorkerTask>("replicationJobs",
                        getModel(), 50) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    protected void populateItem(ListItem<WorkerTask> item) {
                        item.add(new Label("number",
                                Integer.toString(item.getIndex() + 1)));
                    }
                });

        add(new SimplePageableListViewNavigator("pageTableNav", table));
    }

    @SuppressWarnings("unchecked")
    public final IModel<List<WorkerTask>> getModel() {
        return (IModel<List<WorkerTask>>) getDefaultModel();
    }
}
