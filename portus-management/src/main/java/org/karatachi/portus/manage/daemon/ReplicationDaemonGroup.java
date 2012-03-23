package org.karatachi.portus.manage.daemon;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

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
import org.karatachi.portus.manage.AssemblyInfo;
import org.karatachi.portus.manage.daemon.task.ReplicationTask;
import org.karatachi.portus.manage.logic.ReplicationLogic;
import org.karatachi.portus.manage.node.NodeHandler;
import org.karatachi.portus.manage.node.NodeTask;
import org.karatachi.translator.IntervalTranslator;
import org.seasar.framework.container.annotation.tiger.Binding;

public class ReplicationDaemonGroup extends ProducerWorkerDaemonGroup {
    @Binding
    private ReplicationLogic replicationLogic;

    public ReplicationDaemonGroup() {
        super("Replication");
    }

    @Override
    protected ProducerDaemon newProducerInstance(String name) {
        return new ReplicationProducerDaemon(this, name);
    }

    @Override
    protected WorkerDaemon newWorkerInstance(String name) {
        return new ReplicationWorkerDaemon(this, name);
    }

    public void addDerivedReplicationTask(ReplicationTask job) {
        // 正常にレプリケーションが終了したらタスクを２個ReplicationTaskに追加
        List<WorkerTask> tasks = new ArrayList<WorkerTask>();
        for (int i = 0; i < AssemblyInfo.REPLICATION_POWER; ++i) {
            int order =
                    AssemblyInfo.REPLICATION_MINIMAM + job.getOrder()
                            * AssemblyInfo.REPLICATION_POWER + i;
            if (order < job.getReplication()) {
                tasks.add(new ReplicationTask(job, order));
            }
        }

        if (tasks.size() != 0) {
            super.addTask(tasks);
        }
    }

    private class ReplicationProducerDaemon extends ProducerDaemon {
        public ReplicationProducerDaemon(ProducerWorkerDaemonGroup owner,
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
                    replicationLogic.getFileReplicaForReplication();
            for (FileReplication replication : replications) {
                int order = replication.available;
                int max =
                        Math.min(replication.regulated, replication.available
                                * AssemblyInfo.REPLICATION_POWER);
                File file = replicationLogic.getFile(replication.id);
                for (int i = order > 1 ? order : 1; i < max; ++i) {
                    tasks.add(new ReplicationTask(file, replication, i));
                }
            }

            return tasks;
        }
    }

    public class ReplicationWorkerDaemon extends WorkerDaemon implements
            CommandResponseCallback {
        private ReplicationTask job;

        public ReplicationWorkerDaemon(ProducerWorkerDaemonGroup owner,
                String name) {
            super(owner, name);
        }

        @Override
        protected boolean work(WorkerTask task) throws Exception {
            job = (ReplicationTask) task;

            Node source = replicationLogic.getSourceChassis(job.getFile());
            if (source == null) {
                return true;
            }

            Node destination =
                    replicationLogic.getDestinationChassis(job.getFile());
            if (destination == null) {
                return true;
            }

            setDetailText("Work on: %s", destination);

            try {
                NodeHandler handler = NodeHandler.createHandler(destination);
                try {
                    handler.executeDDDTask(new ReplicationCommandTask(this,
                            destination, source));
                } finally {
                    handler.close();
                }
                addDerivedReplicationTask(job);
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

        public class ReplicationCommandTask extends NodeTask {
            private final CommandResponseCallback callback;
            private final Node destination;
            private final Node source;

            public ReplicationCommandTask(CommandResponseCallback callback,
                    Node destination, Node source) {
                this.callback = callback;
                this.destination = destination;
                this.source = source;
            }

            @Override
            public void execute(CommandHandler handler) throws IOException {
                setDetailText("Replication Started: file_id=%d",
                        job.getFile().id);

                CommandResponse response =
                        handler.executeCommand(String.format(
                                "wget %s %s %s %s", source.ipAddress,
                                source.httpPort, job.getFile().id,
                                job.getFile().name), callback);

                if (response.getStatusCode() == CommandResponse.S_OK
                        && response.getHeaderLong("Content-Length") == job.getSize()) {
                    replicationLogic.commit(job.getFile(), destination.id,
                            source.id, response.getHeader("Content-MD5"));
                    setDetailText(
                            "Replication Succeeded: file_id=%d, file_size=%d %s",
                            job.getFile().id, job.getSize(),
                            response.getStatusText());
                } else {
                    String message =
                            "コピー失敗: [src=%s, dest=%s, file=%s, size=%s, transferred=%s] (%s) %s";
                    logger.warn(String.format(message, source.id,
                            destination.id, job.getFile().id, job.getSize(),
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
