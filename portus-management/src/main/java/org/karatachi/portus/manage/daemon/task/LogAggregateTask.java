package org.karatachi.portus.manage.daemon.task;

import java.util.Calendar;

import org.karatachi.daemon.producer.WorkerTask;

public class LogAggregateTask implements WorkerTask {
    public enum Type {
        COUNT, DUMP
    }

    private final Calendar cal;
    private final Type type;

    public LogAggregateTask(Calendar cal, Type type) {
        this.cal = cal;
        this.type = type;
    }

    public Calendar getCal() {
        return cal;
    }

    public Type getType() {
        return type;
    }

    @Override
    public int compareTo(WorkerTask o) {
        if (o instanceof LogAggregateTask) {
            int ret = 0;
            if ((ret = type.compareTo(type)) != 0) {
                return ret;
            } else {
                return cal.compareTo(((LogAggregateTask) o).cal);
            }
        } else {
            return getClass().getName().compareTo(o.getClass().getName());
        }
    }
}
