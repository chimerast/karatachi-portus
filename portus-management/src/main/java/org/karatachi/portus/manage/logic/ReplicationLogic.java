package org.karatachi.portus.manage.logic;

import java.util.List;

import org.karatachi.portus.core.dao.FileDao;
import org.karatachi.portus.core.dao.FileReplicationDao;
import org.karatachi.portus.core.dao.NodeDao;
import org.karatachi.portus.core.dao.StoredinfoDao;
import org.karatachi.portus.core.entity.File;
import org.karatachi.portus.core.entity.FileReplication;
import org.karatachi.portus.core.entity.Node;
import org.karatachi.portus.core.entity.Storedinfo;
import org.karatachi.portus.manage.AssemblyInfo;
import org.karatachi.system.SystemInfo;
import org.seasar.framework.container.annotation.tiger.Binding;
import org.seasar.framework.exception.SRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReplicationLogic {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Binding
    private NodeDao nodeDao;
    @Binding
    private FileDao fileDao;
    @Binding
    private FileReplicationDao fileReplicationDao;
    @Binding
    private StoredinfoDao storedinfoDao;

    public FileReplication getFileReplication(File file) {
        return fileReplicationDao.select(file.id);
    }

    public List<FileReplication> getFileReplicaForReplication() {
        return fileReplicationDao.selectForReplication();
    }

    public List<FileReplication> getFileReplicaForDirectReplication() {
        return fileReplicationDao.selectForDirectReplication(AssemblyInfo.REPLICATION_MINIMAM);
    }

    public long getCurrentMaxFileId() {
        return fileReplicationDao.selectMaxId();
    }

    public File getFile(long fileId) {
        return fileDao.select(fileId);
    }

    public Node getDestinationChassis(File file) {
        switch ((int) file.fileTypeId) {
        case File.TYPE_FLASH_STREAMING:
            return nodeDao.selectReplicationDestinationWithType(file.id,
                    Node.TYPE_FLASH_MEDIA_SERVER);
        default:
            return nodeDao.selectReplicationDestination(file.id);
        }
    }

    public Node getSourceChassis(File file) {
        return nodeDao.selectReplicationSource(file.id);
    }

    public void commit(File file, long destChassisId, long srcChassisId,
            String digest) {
        try {
            Storedinfo storedinfo = new Storedinfo();
            storedinfo.fileId = file.id;
            storedinfo.nodeId = destChassisId;
            storedinfo.fromNodeId = srcChassisId;
            storedinfo.hostname = SystemInfo.HOST_NAME;
            storedinfo.digest = digest;
            storedinfoDao.insert(storedinfo);
        } catch (SRuntimeException ignore) {
            logger.warn(ignore.getMessage());
        }
    }

    public void rollback(Storedinfo storedinfo) {
        storedinfoDao.delete(storedinfo);
    }
}
