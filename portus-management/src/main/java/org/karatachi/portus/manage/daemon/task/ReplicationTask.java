package org.karatachi.portus.manage.daemon.task;

import org.karatachi.daemon.producer.WorkerTask;
import org.karatachi.portus.core.entity.File;
import org.karatachi.portus.core.entity.FileReplication;
import org.karatachi.portus.manage.AssemblyInfo;

public class ReplicationTask implements WorkerTask {
    private static final double PRIORITY_BASE = Math.log(128);

    private final File file;
    private final String url;
    private final long size;
    private final int order;
    private final int replication;

    private final double priority;

    public ReplicationTask(File file, FileReplication replication, int order) {
        this.file = file;
        this.url = file.fullPath;
        this.size = file.size;
        this.replication = replication.regulated;
        this.order = order;
        if (size == 0) {
            this.priority = 100.0;
        } else {
            this.priority = order + Math.log(size + 1) / PRIORITY_BASE;
        }
    }

    public ReplicationTask(ReplicationTask task, int order) {
        this.file = task.file;
        this.url = task.url;
        this.size = task.size;
        this.replication = task.replication;
        this.order = order;
        if (size == 0) {
            this.priority = 100.0;
        } else {
            this.priority = order + Math.log(size + 1) / PRIORITY_BASE;
        }
    }

    public File getFile() {
        return file;
    }

    public String getUrl() {
        return url;
    }

    public long getSize() {
        return size;
    }

    public int getReplication() {
        return replication;
    }

    public int getOrder() {
        return order;
    }

    public double getPriority() {
        return priority;
    }

    @Override
    public int compareTo(WorkerTask o) {
        ReplicationTask r = (ReplicationTask) o;

        if (this.file.id == r.file.id && this.order == r.order) {
            return 0;
        } else if (this.order <= AssemblyInfo.REPLICATION_MINIMAM
                && r.order > AssemblyInfo.REPLICATION_MINIMAM) {
            return -1;
        } else if (this.order > AssemblyInfo.REPLICATION_MINIMAM
                && r.order <= AssemblyInfo.REPLICATION_MINIMAM) {
            return 1;
        } else {
            if (this.priority < r.priority) {
                return -1;
            }
            if (this.priority > r.priority) {
                return 1;
            }
            return (int) (this.file.id - r.file.id);
        }
    }
}
