package org.karatachi.portus.api;

import java.util.Map;

import javax.servlet.http.HttpSession;

public interface WebPostAPI {
    public Object exec(String path, Map<String, String[]> params, String body,
            HttpSession session);

    public String getName();
}
