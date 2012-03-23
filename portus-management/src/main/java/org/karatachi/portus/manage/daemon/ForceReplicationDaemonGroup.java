package org.karatachi.portus.manage.daemon;

import java.util.ArrayList;
import java.util.List;

import org.karatachi.daemon.producer.ProducerDaemon;
import org.karatachi.daemon.producer.WorkerTask;
import org.karatachi.portus.core.entity.File;
import org.karatachi.portus.core.entity.FileReplication;
import org.karatachi.portus.manage.daemon.task.ReplicationTask;

public class ForceReplicationDaemonGroup extends
        AbstractDirectReplicationDaemonGroup {
    public ForceReplicationDaemonGroup() {
        super("ForceReplication");
    }

    public void addTask(long fileId, long count) {
        List<WorkerTask> tasks = new ArrayList<WorkerTask>();

        File file = replicationLogic.getFile(fileId);
        FileReplication replication = replicationLogic.getFileReplication(file);

        int order = replication.available;
        for (int i = order; i < count; ++i) {
            tasks.add(new ReplicationTask(file, replication, i));
        }

        addTask(tasks);
    }

    @Override
    public void clearTask() {
        super.clearTask();
    }

    @Override
    protected ProducerDaemon newProducerInstance(String name) {
        return new ProducerDaemon(this, name) {
            @Override
            protected List<? extends WorkerTask> replaceTask() {
                return null;
            }
        };
    }
}
