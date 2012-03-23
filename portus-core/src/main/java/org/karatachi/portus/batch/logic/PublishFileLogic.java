package org.karatachi.portus.batch.logic;

import org.karatachi.portus.batch.ProcessLogic;
import org.karatachi.portus.core.dao.FileDao;
import org.karatachi.portus.core.entity.File;
import org.karatachi.portus.core.logic.FileAccessLogic;
import org.seasar.framework.container.annotation.tiger.Binding;

public class PublishFileLogic extends ProcessLogic {
    @Binding
    private FileDao fileDao;
    @Binding
    private FileAccessLogic fileAccessLogic;

    @Override
    public void run() {
        if (args.length != 1) {
            throw new IllegalArgumentException("引数の数が違います。");
        }
        publishFile(args[0]);
    }

    public void publishFile(String source) {
        String[] sourcedirs = source.split("/");

        File dir = fileDao.selectFileInDirectory(0, sourcedirs[1]);
        if (dir == null) {
            throw new IllegalArgumentException("属性を変更するフォルダがありません。");
        }

        int i = 2;
        while (i < sourcedirs.length) {
            dir = fileDao.selectFileInDirectory(dir.id, sourcedirs[i]);
            if (dir == null) {
                throw new IllegalArgumentException("属性を変更するフォルダがありません。");
            }
            ++i;
        }

        fileAccessLogic.setAttributeRecursive(dir, "published", true);
    }
}
