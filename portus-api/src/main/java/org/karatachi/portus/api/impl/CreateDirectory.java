package org.karatachi.portus.api.impl;

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.karatachi.portus.api.WebGetAPI;
import org.karatachi.portus.api.type.CreateDirectoryResponse;
import org.karatachi.portus.core.entity.File;
import org.karatachi.portus.core.logic.FileAccessLogic;
import org.seasar.framework.container.annotation.tiger.Binding;

public class CreateDirectory implements WebGetAPI {
    @Binding
    private FileAccessLogic fileAccessLogic;

    @Override
    public Object exec(String path, Map<String, String[]> params,
            HttpSession session) {
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

        File file = fileAccessLogic.createDirByPathTx(path, attributes);
        return new CreateDirectoryResponse(file.fullPath);
    }

    @Override
    public String getName() {
        return "mkdir";
    }
}
