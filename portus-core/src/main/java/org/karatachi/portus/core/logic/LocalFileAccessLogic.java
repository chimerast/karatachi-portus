package org.karatachi.portus.core.logic;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.karatachi.portus.core.PortusRuntimeException;
import org.karatachi.portus.core.auth.Authorize;
import org.karatachi.portus.core.dto.AccountDto;
import org.karatachi.portus.core.entity.LocalFile;
import org.karatachi.portus.core.type.AccountRole;
import org.seasar.framework.container.annotation.tiger.Binding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalFileAccessLogic {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Binding
    private AccountDto accountDto;

    @Authorize(AccountRole.Bit.VIEW_FILE)
    public LocalFile getAccountRoot() {
        LocalFile ret = new LocalFile();
        ret.file = new File(accountDto.getAccount().homedir);
        return ret;
    }

    @Authorize(AccountRole.Bit.VIEW_FILE)
    public LocalFile getParentDir(LocalFile dir) {
        if (dir.file.equals(getAccountRoot().file)) {
            return null;
        }
        LocalFile parent = new LocalFile();
        parent.file = dir.file.getParentFile();
        return parent;
    }

    public LocalFile getFileInDirectory(LocalFile dir, String name) {
        LocalFile ret = new LocalFile();
        ret.file = new File(dir.file, name);
        if (ret.file.exists()) {
            return ret;
        } else {
            throw new PortusRuntimeException("ファイルが見つかりません。",
                    HttpURLConnection.HTTP_NOT_FOUND);
        }
    }

    @Authorize(AccountRole.Bit.VIEW_FILE)
    public List<LocalFile> getFiles(LocalFile dir) {
        List<LocalFile> ret = new ArrayList<LocalFile>();
        if (!dir.file.isDirectory()) {
            return ret;
        }
        for (File file : dir.file.listFiles()) {
            LocalFile localfile = new LocalFile();
            localfile.file = file;
            ret.add(localfile);
        }
        Collections.sort(ret, new Comparator<LocalFile>() {
            @Override
            public int compare(LocalFile o1, LocalFile o2) {
                if (o1.file.isDirectory() && !o2.file.isDirectory()) {
                    return -1;
                } else if (!o1.file.isDirectory() && o2.file.isDirectory()) {
                    return 1;
                } else {
                    return o1.file.getName().compareTo(o2.file.getName());
                }
            }
        });
        return ret;
    }

    public LocalFile getFileByPath(String path) {
        LocalFile current = getAccountRoot();
        for (String name : path.split("/")) {
            if (name.equals("") || name.equals(".") || name.equals("..")) {
                continue;
            } else {
                current = getFileInDirectory(current, name);
                if (current == null) {
                    throw new PortusRuntimeException("ファイルが見つかりません。",
                            HttpURLConnection.HTTP_NOT_FOUND);
                }
            }
        }
        return current;
    }
}
