package org.karatachi.portus.api.impl;

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.karatachi.portus.api.WebGetAPI;
import org.karatachi.portus.core.logic.FileAccessLogic;
import org.seasar.framework.container.annotation.tiger.Binding;

public class RemoveFile implements WebGetAPI {
    @Binding
    private FileAccessLogic fileAccessLogic;

    @Override
    public Object exec(String path, Map<String, String[]> params,
            HttpSession session) {
        fileAccessLogic.removeFileByPathTx(path);
        return true;
    }

    @Override
    public String getName() {
        return "remove";
    }
}
