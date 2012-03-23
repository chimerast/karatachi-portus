package org.karatachi.portus.api.impl;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.karatachi.portus.api.WebGetAPI;
import org.karatachi.portus.core.PortusRuntimeException;
import org.karatachi.portus.core.entity.File;
import org.karatachi.portus.core.entity.LocalFile;
import org.karatachi.portus.core.logic.FileAccessLogic;
import org.karatachi.portus.core.logic.LocalFileAccessLogic;
import org.seasar.framework.container.annotation.tiger.Binding;

public class RegisterFile implements WebGetAPI {
    @Binding
    private FileAccessLogic fileAccessLogic;
    @Binding
    private LocalFileAccessLogic localFileAccessLogic;

    @Override
    public Object exec(String path, Map<String, String[]> params,
            HttpSession session) {
        String source = params.get("source")[0];

        File attributes = new File();
        if (params.containsKey("published")) {
            if ("1".equals(params.get("published")[0])) {
                attributes.published = true;
            }
        }
        if (params.containsKey("authorized")) {
            if ("1".equals(params.get("authorized")[0])) {
                attributes.authorized = true;
            }
        }

        int idx = path.lastIndexOf("/");
        File dir =
                fileAccessLogic.createDirByPathTx(path.substring(0, idx + 1),
                        attributes);
        LocalFile original = localFileAccessLogic.getFileByPath(source);
        try {
            fileAccessLogic.registerFileWithNameTx(dir, original,
                    path.substring(idx + 1), attributes);
        } catch (IOException e) {
            throw new PortusRuntimeException("ファイルの移動に失敗しました。",
                    HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
        return true;
    }

    @Override
    public String getName() {
        return "register";
    }
}
