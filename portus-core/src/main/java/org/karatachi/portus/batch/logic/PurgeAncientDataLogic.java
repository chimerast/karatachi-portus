package org.karatachi.portus.batch.logic;

import java.io.File;

import org.karatachi.portus.batch.ProcessLogic;
import org.karatachi.portus.common.crypto.EncryptionUtils;
import org.karatachi.portus.core.AssemblyInfo;
import org.karatachi.portus.core.dao.ConfigDao;
import org.karatachi.portus.core.dao.FileDao;
import org.seasar.framework.container.annotation.tiger.Binding;

public class PurgeAncientDataLogic extends ProcessLogic {

    @Binding
    private ConfigDao configDao;
    @Binding
    private FileDao fileDao;

    private static final String lastProcessed = "purge.lastProcessed";

    @Override
    public void run() {
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

        long max = fileDao.selectMaxId();
        for (long i = max; i > 0; --i) {
            org.karatachi.portus.core.entity.File file = fileDao.select(i);
            if (file == null || file.directory) {
                File fs =
                        new File(AssemblyInfo.PATH_REG_DATA,
                                EncryptionUtils.toFilePath(i));
                if (fs.exists()) {
                    boolean result = fs.delete();
                    logger.info("Purged id={}: {}", i, result ? "success"
                            : "failure");
                }
            } else {
                logger.info("Live id={}", i);
            }
            configDao.update(lastProcessed, Long.toString(i));
        }
    }
}
