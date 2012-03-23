package org.karatachi.portus.api.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.karatachi.portus.api.WebPostAPI;
import org.karatachi.portus.api.type.GetFileStatusRequest;
import org.karatachi.portus.core.AssemblyInfo;
import org.karatachi.portus.core.entity.File;
import org.karatachi.portus.core.entity.FileReplication;
import org.karatachi.portus.core.logic.FileAccessLogic;
import org.seasar.framework.container.annotation.tiger.Binding;

public class GetFileStatus implements WebPostAPI {
    @Binding
    private FileAccessLogic fileAccessLogic;

    @Override
    public Object exec(String path, Map<String, String[]> params, String body,
            HttpSession session) {
        JSONArray req = JSONArray.fromObject(body);
        JSONArray ret = new JSONArray();

        GetFileStatusRequest[] array =
                (GetFileStatusRequest[]) JSONArray.toArray(req,
                        GetFileStatusRequest.class);

        List<String> paths = new ArrayList<String>();
        for (GetFileStatusRequest st : array) {
            JSONObject obj = new JSONObject();
            obj.put("idx", st.getIdx());
            ret.add(obj);
            paths.add(st.getPath());
        }

        List<File> files = new ArrayList<File>();
        for (String p : paths) {
            files.add(fileAccessLogic.findPath(p));
        }

        if (params.containsKey("command")
                && "set".equalsIgnoreCase(params.get("command")[0])) {
            for (File file : files) {
                boolean modified = false;
                if (params.containsKey("published")) {
                    boolean publish = "1".equals(params.get("published")[0]);
                    if (file.published != publish) {
                        file.published = publish;
                        modified = true;
                    }
                }
                if (params.containsKey("authorized")) {
                    boolean auth = "1".equals(params.get("authorized")[0]);
                    if (file.authorized != auth) {
                        file.authorized = auth;
                        modified = true;
                    }
                }
                if (modified) {
                    fileAccessLogic.updateFileTx(file);
                }
            }
        }

        for (int i = 0; i < ret.size(); ++i) {
            File file = files.get(i);
            if (file != null) {
                ((JSONObject) ret.get(i)).put("status", getFileStatus(file));
                if (params.containsKey("published")) {
                    ((JSONObject) ret.get(i)).put("published", file.published
                            ? 1 : 0);
                }
                if (params.containsKey("authorized")) {
                    ((JSONObject) ret.get(i)).put("authorized", file.authorized
                            ? 1 : 0);
                }
                if (params.containsKey("md5")) {
                    ((JSONObject) ret.get(i)).put("md5", file.digest);
                }
            } else {
                ((JSONObject) ret.get(i)).put("status", -1);
            }
        }

        return ret;
    }

    private int getFileStatus(File file) {
        FileReplication replication = fileAccessLogic.getReplication(file);
        if (replication.available < AssemblyInfo.REPLICATION_MINIMAM) {
            return 0;
        } else if (replication.available < AssemblyInfo.REPLICATION_THRESHOLD) {
            return 1;
        } else {
            return 2;
        }
    }

    @Override
    public String getName() {
        return "status";
    }
}
