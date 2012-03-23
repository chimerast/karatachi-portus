package org.karatachi.portus.api;

import java.io.IOException;
import java.net.URLEncoder;

import net.sf.json.JSONArray;

import org.karatachi.portus.api.type.GetFileStatusRequest;
import org.karatachi.portus.api.type.GetFileStatusResponse;
import org.karatachi.portus.api.type.ResponseContainer;
import org.karatachi.portus.api.type.SearchFileResponse;
import org.karatachi.portus.client.AssemblyInfo;

public class PortusAPIConnector extends APIConnector {
    public PortusAPIConnector(String domain) {
        super(AssemblyInfo.REST_API_URL, domain);
    }

    public boolean registerFile(String name, String ftpname) throws IOException {
        String path = domain + "/" + name;
        path +=
                String.format("?source=%s&published=1", URLEncoder.encode(
                        ftpname, "UTF-8"));
        ResponseContainer res = doGetRequest("register", path, Object.class);
        logger.debug("registerFile: result=" + res.getResponse());
        return res.isSuccess();
    }

    public boolean createDirectory(String name) throws IOException {
        String path = domain + "/" + name + "?published=1";
        ResponseContainer res = doGetRequest("mkdir", path, Object.class);
        return res.isSuccess();
    }

    public boolean removeFile(String name) throws IOException {
        String path = domain + "/" + name;
        ResponseContainer res = doGetRequest("remove", path, Object.class);
        return res.isSuccess();
    }

    public long[] getFileStatus(String[] files) throws IOException {
        GetFileStatusRequest[] request = new GetFileStatusRequest[files.length];
        for (int i = 0; i < files.length; ++i) {
            request[i] =
                    new GetFileStatusRequest(normalizePath(domain + "/"
                            + files[i]), i);
        }

        ResponseContainer res =
                doPostRequest("status", JSONArray.fromObject(request),
                        GetFileStatusResponse.class);

        long[] ret = new long[files.length];
        for (GetFileStatusResponse status : (GetFileStatusResponse[]) res.getResponse()) {
            ret[(int) status.getIdx()] = status.getStatus();
        }

        return ret;
    }

    public SearchFileResponse[] search(String name) throws IOException {
        String path = domain + "/" + name;
        ResponseContainer res =
                doGetRequest("list", path, SearchFileResponse.class);
        return (SearchFileResponse[]) res.getResponse();
    }
}
