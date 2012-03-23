package org.karatachi.portus.manage.daemon.task;

import org.karatachi.daemon.producer.WorkerTask;
import org.karatachi.portus.core.dto.FileDigestDto;

public class DigestCalculateTask implements WorkerTask {
    private final FileDigestDto dto;

    public DigestCalculateTask(FileDigestDto dto) {
        this.dto = dto;
    }

    public FileDigestDto getDto() {
        return dto;
    }

    @Override
    public int compareTo(WorkerTask o) {
        if (o instanceof DigestCalculateTask) {
            return (int) (dto.getId() - ((DigestCalculateTask) o).getDto().getId());
        } else {
            return getClass().getName().compareTo(o.getClass().getName());
        }
    }
}
