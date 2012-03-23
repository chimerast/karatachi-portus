package org.karatachi.portus.api.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.karatachi.portus.api.WebGetAPI;
import org.karatachi.portus.api.type.SearchFileResponse;
import org.karatachi.portus.core.entity.File;
import org.karatachi.portus.core.logic.FileAccessLogic;
import org.seasar.framework.container.SingletonS2Container;

public class ListFile implements WebGetAPI {
    @Override
    public Object exec(String path, Map<String, String[]> params,
            HttpSession session) {

        List<SearchFileResponse> ret = new ArrayList<SearchFileResponse>();

        Date registeredBefore = getDate(params, "registered_before");
        Date registeredAfter = getDate(params, "registered_after");

        FileAccessLogic fileAccessLogic =
                SingletonS2Container.getComponent(FileAccessLogic.class);

        for (File file : fileAccessLogic.search(path, registeredBefore,
                registeredAfter)) {
            ret.add(new SearchFileResponse(file));
        }

        return ret;
    }

    private Date getDate(Map<String, String[]> params, String key) {
        try {
            if (params.containsKey(key)) {
                return new Date(Long.parseLong(params.get(key)[0]));
            }
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public String getName() {
        return "list";
    }
}
