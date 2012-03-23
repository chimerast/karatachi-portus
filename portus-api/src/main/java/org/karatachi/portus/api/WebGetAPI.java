package org.karatachi.portus.api;

import java.util.Map;

import javax.servlet.http.HttpSession;

public interface WebGetAPI {
    public Object exec(String path, Map<String, String[]> params,
            HttpSession session);

    public String getName();
}
