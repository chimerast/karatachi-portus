package org.karatachi.portus.manage.daemon;

import java.util.ArrayList;
import java.util.List;

import org.karatachi.daemon.Daemon;
import org.karatachi.daemon.producer.WorkerTask;
import org.karatachi.portus.core.AssemblyInfo;
import org.karatachi.portus.core.entity.File;
import org.karatachi.portus.core.entity.FileReplication;
import org.karatachi.portus.manage.daemon.task.ReplicationTask;
import org.karatachi.translator.IntervalTranslator;

public class DirectReplicationDaemonGroup extends
        AbstractDirectReplicationDaemonGroup {
    private NewCommingFileDaemon newCommingFileDaemon;

    public DirectReplicationDaemonGroup() {
        super("DirectReplication");
        newCommingFileDaemon = new NewCommingFileDaemon();
    }

    @Override
    protected synchronized void initialize() {
        super.initialize();
        newCommingFileDaemon.startup();
    }

    @Override
    protected synchronized void cleanup() throws InterruptedException {
        newCommingFileDaemon.shutdown();
        super.cleanup();
    }

    public class NewCommingFileDaemon extends Daemon {
        private long prevMaxId = -1L;

        public NewCommingFileDaemon() {
            super("NewCommingFileDaemon");
        }

        @Override
        protected void updateNextRun() {
            setNextRun(getLastStarted() + IntervalTranslator.sec(10));
        }

        @Override
        protected void work() throws Exception {
            long newMaxId = replicationLogic.getCurrentMaxFileId();
            if (prevMaxId != -1L) {
                List<WorkerTask> tasks = new ArrayList<WorkerTask>();
                for (long i = prevMaxId + 1; i <= newMaxId; ++i) {
                    File file = replicationLogic.getFile(i);
                    if (file == null || file.directory) {
                        continue;
                    }
                    FileReplication replication =
                            replicationLogic.getFileReplication(file);
                    if (replication == null) {
                        continue;
                    }
                    for (int j = 0; j < AssemblyInfo.REPLICATION_MINIMAM; ++j) {
                        tasks.add(new ReplicationTask(file, replication, j));
                    }
                }
                addTask(tasks);
            }
            prevMaxId = newMaxId;
        }
    }
}
