package org.karatachi.portus.manage.daemon;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import org.karatachi.daemon.DaemonManager;
import org.karatachi.daemon.producer.ProducerDaemon;
import org.karatachi.daemon.producer.ProducerWorkerDaemonGroup;
import org.karatachi.daemon.producer.WorkerDaemon;
import org.karatachi.daemon.producer.WorkerTask;
import org.karatachi.net.shell.CommandHandler;
import org.karatachi.net.shell.CommandResponse;
import org.karatachi.net.shell.CommandResponseCallback;
import org.karatachi.portus.core.entity.File;
import org.karatachi.portus.core.entity.FileReplication;
import org.karatachi.portus.core.entity.Node;
import org.karatachi.portus.core.logic.FileAccessLogic;
import org.karatachi.portus.manage.AssemblyInfo;
import org.karatachi.portus.manage.daemon.task.ReplicationTask;
import org.karatachi.portus.manage.logic.ReplicationLogic;
import org.karatachi.portus.manage.node.NodeHandler;
import org.karatachi.portus.manage.node.NodeTask;
import org.karatachi.translator.IntervalTranslator;
import org.seasar.framework.container.annotation.tiger.Binding;

public abstract class AbstractDirectReplicationDaemonGroup extends
        ProducerWorkerDaemonGroup {
    @Binding
    protected ReplicationLogic replicationLogic;
    @Binding
    private FileAccessLogic fileAccessLogic;

    public AbstractDirectReplicationDaemonGroup(String name) {
        super(name);
    }

    @Override
    protected ProducerDaemon newProducerInstance(String name) {
        return new DirectReplicationProducerDaemon(this, name);
    }

    @Override
    protected WorkerDaemon newWorkerInstance(String name) {
        return new DirectReplicationWorkerDaemon(this, name);
    }

    private class DirectReplicationProducerDaemon extends ProducerDaemon {
        public DirectReplicationProducerDaemon(ProducerWorkerDaemonGroup owner,
                String name) {
            super(owner, name);
        }

        @Override
        protected void updateNextRun() {
            setNextRun(getLastStarted() + IntervalTranslator.min(3));
        }

        @Override
        protected List<? extends WorkerTask> replaceTask() {
            List<WorkerTask> tasks = new ArrayList<WorkerTask>();
            List<FileReplication> replications =
                    replicationLogic.getFileReplicaForDirectReplication();
            for (FileReplication replication : replications) {
                int order = replication.available;
                for (int i = order; i < AssemblyInfo.REPLICATION_MINIMAM; ++i) {
                    File file = replicationLogic.getFile(replication.id);
                    tasks.add(new ReplicationTask(file, replication, i));
                }
            }
            return tasks;
        }
    }

    public class DirectReplicationWorkerDaemon extends WorkerDaemon implements
            CommandResponseCallback {
        private ReplicationTask job;

        public DirectReplicationWorkerDaemon(ProducerWorkerDaemonGroup owner,
                String name) {
            super(owner, name);
        }

        @Override
        protected boolean work(WorkerTask task) throws Exception {
            job = (ReplicationTask) task;

            Node destination =
                    replicationLogic.getDestinationChassis(job.getFile());
            if (destination == null) {
                return true;
            }

            setDetailText("Work on: %s", destination);

            try {
                NodeHandler handler = NodeHandler.createHandler(destination);
                try {
                    handler.executeDDDTask(new DirectReplicationCommandTask(
                            this, destination));
                } finally {
                    handler.close();
                }

                // 正常にレプリケーションが終了したらタスクを２個ReplicationTaskに追加
                ReplicationDaemonGroup replicationDaemonGroup =
                        DaemonManager.getDaemonGroup(ReplicationDaemonGroup.class);
                if (replicationDaemonGroup != null) {
                    replicationDaemonGroup.addDerivedReplicationTask(job);
                }

                return true;
            } catch (SocketTimeoutException e) {
                logger.trace("接続失敗: " + destination);
                return false;
            } catch (ConnectException e) {
                logger.trace("接続失敗: " + destination);
                return false;
            } catch (IOException e) {
                logger.trace("転送失敗: " + destination);
                return true;
            }
        }

        @Override
        public void callbackStatus(int statusCode, String statusText) {
            setDetailText("Replication Running: file_id=%d %s",
                    job.getFile().id, statusText);
        }

        public class DirectReplicationCommandTask extends NodeTask {
            private final CommandResponseCallback callback;
            private final Node destination;

            public DirectReplicationCommandTask(
                    CommandResponseCallback callback, Node destination) {
                this.callback = callback;
                this.destination = destination;
            }

            @Override
            public void execute(CommandHandler handler) throws IOException {
                setDetailText("Replication Started: file_id=%d",
                        job.getFile().id);

                CommandResponse response =
                        handler.executeCommand(String.format(
                                "wget %s %s %s %s", AssemblyInfo.DATA_SERVER,
                                80, job.getFile().id, job.getFile().name),
                                callback);

                logger.warn(String.format("wget %s %s %s",
                        AssemblyInfo.DATA_SERVER, 80, job.getFile().id));

                if (response.getStatusCode() == CommandResponse.S_OK
                        && response.getHeaderLong("Content-Length") == job.getSize()) {
                    replicationLogic.commit(job.getFile(), destination.id,
                            Node.DATACENTER_ID,
                            response.getHeader("Content-MD5"));
                    setDetailText(
                            "Replication Succeeded: file_id=%d, file_size=%d %s",
                            job.getFile().id, job.getSize(),
                            response.getStatusText());
                } else {
                    if (response.getStatusCode() == CommandResponse.S_OK
                            && response.getHeaderLong("Content-Length") != job.getSize()) {
                        fileAccessLogic.updateFileDigest(job.getFile().id,
                                job.getSize(), null);
                    }

                    String message =
                            "コピー失敗: [src=-1, dest=%012X, file=%s, size=%s, transferred=%s] (%s) %s";
                    logger.warn(String.format(message, destination.id,
                            job.getFile().id, job.getSize(),
                            response.getHeaderLong("Content-Length"),
                            response.getStatusCode(), response.getStatusText()));
                    setDetailText(
                            "Replication Failed: file_id=%d, file_size=%d %s",
                            job.getFile().id, job.getSize(),
                            response.getStatusText());
                    throw new IOException(message);
                }
            }

            @Override
            public boolean isSupported(int revision) {
                return revision >= 4;
            }
        }
    }
}
