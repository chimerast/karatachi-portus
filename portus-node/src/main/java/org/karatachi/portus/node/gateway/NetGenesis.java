package org.karatachi.portus.node.gateway;

import java.io.IOException;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;

public class NetGenesis {
    public String baseUrl;
    public Credentials credentials;

    public NetGenesis(String ipAddr) {
        this.baseUrl = "http://" + ipAddr + "/";
        this.credentials = null;
    }

    public boolean authorize(String username, String password) {
        Credentials credentials =
                new UsernamePasswordCredentials(username, password);

        HttpClient client = new HttpClient();
        client.getParams().setParameter("http.connection.timeout",
                new Integer(3000));

        client.getState().setCredentials(AuthScope.ANY, credentials);

        HttpMethod method = new GetMethod(baseUrl);
        try {
            if (client.executeMethod(method) == 200) {
                this.credentials = credentials;
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            return false;
        } finally {
            method.releaseConnection();
        }
    }

    public boolean setMtu(int mtu) {
        PostMethod method = new PostMethod(baseUrl);
        method.setRequestHeader("Content-Type",
                "application/x-www-form-urlencoded; charset=Shift_JIS");
        method.addParameter("jp-pppoedychange_AccountPassword", "ＰＡＳＳＷＯＲＤ");
        method.addParameter("jp-pppoedychange_AccountRePassword", "ＰＡＳＳＷＯＲＤ");
        method.addParameter("jp-pppoedychange_MssClampFlag", "on");
        method.addParameter("jp-pppoedychange_Mtu", Integer.toString(mtu));
        method.addParameter("jp-pppoedychange_ConnectMode", "always_connect");
        method.addParameter("jp-pppoedychange_KeepAliveUse", "on");
        method.addParameter("jp-pppoedychange_KeepAliveCount", "6");
        method.addParameter("jp-pppoedychange_KeepAliveInterval", "0:00:30");
        method.addParameter("jp-pppoedychange_KeepTime", "00:03:00");
        method.addParameter("jp-pppoedychange_L4APPptp", "on");
        method.addParameter("jp-pppoedychange_Advantage", "on");
        method.addParameter("jp-pppoedychange_Set", "");
        return executeCommand(method);
    }

    public boolean updatePPPoE() {
        PostMethod method = new PostMethod(baseUrl);
        method.addParameter("jp-pppoeport_Set", "");
        return executeCommand(method);
    }

    public boolean addIpMasquaradePPPoE(String ipAddr, String port) {
        PostMethod method = new PostMethod(baseUrl);
        method.addParameter("jp-pppoe1_ipmasq_e_Protocol", "tcp");
        method.addParameter("jp-pppoe1_ipmasq_e_IpAddr", ipAddr);
        method.addParameter("jp-pppoe1_ipmasq_e_StartPort", port);
        method.addParameter("jp-pppoe1_ipmasq_e_EndPort", port);
        method.addParameter("jp-pppoe1_ipmasq_e_Add", "");
        return executeCommand(method);
    }

    public boolean addIpMasquaradeWan(String ipAddr, String port) {
        PostMethod method = new PostMethod(baseUrl);
        method.addParameter("jp-wan_ipmasq_e_Protocol", "tcp");
        method.addParameter("jp-wan_ipmasq_e_IpAddr", ipAddr);
        method.addParameter("jp-wan_ipmasq_e_StartPort", port);
        method.addParameter("jp-wan_ipmasq_e_EndPort", port);
        method.addParameter("jp-wan_ipmasq_e_Add", "");
        return executeCommand(method);
    }

    public boolean updateIpMasquarade() {
        PostMethod method = new PostMethod(baseUrl);
        method.addParameter("jp-ipmasq_Set", "");
        return executeCommand(method);
    }

    public boolean setPassword(String username, String password) {
        PostMethod method = new PostMethod(baseUrl);
        method.addParameter("jp-security_AdminId", username);
        method.addParameter("jp-security_AdminPassword", password);
        method.addParameter("jp-security_ReAdminPassword", password);
        method.addParameter("jp-security_UserId", "");
        method.addParameter("jp-security_UserPassword", "");
        method.addParameter("jp-security_ReUserPassword", "");
        method.addParameter("jp-security_Set", "");
        return executeCommand(method);
    }

    public boolean removeIpMasquaradePPPoE() {
        PostMethod method = new PostMethod(baseUrl);
        method.addParameter("jp-pppoe1_ipmasq_Del_1", "");
        for (int i = 0; i < 32; i++) {
            String responseBody = executeCommandWithResponse(method);
            if (responseBody != null) {
                if (responseBody.indexOf("jp-pppoe1_ipmasq_Del_1") == -1) {
                    return true;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    public boolean removeIpMasquaradeWan() {
        PostMethod method = new PostMethod(baseUrl);
        method.addParameter("jp-wan_ipmasq_Del_1", "");
        for (int i = 0; i < 32; i++) {
            String responseBody = executeCommandWithResponse(method);
            if (responseBody != null) {
                if (responseBody.indexOf("jp-wan_ipmasq_Del_1") == -1) {
                    return true;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    public boolean save() {
        PostMethod method = new PostMethod(baseUrl);
        method.addParameter("SAVE", "");
        return executeCommand(method);
    }

    public boolean reboot() {
        PostMethod method = new PostMethod(baseUrl);
        method.addParameter("REBOOT", "");
        return executeCommand(method);
    }

    private boolean executeCommand(PostMethod method) {
        HttpClient client = new HttpClient();
        client.getState().setCredentials(AuthScope.ANY, credentials);
        client.getParams().setParameter("http.connection.timeout",
                new Integer(3000));
        client.getParams().setParameter("http.socket.timeout",
                new Integer(5000));
        client.getParams().setParameter("http.protocol.content-charset",
                "Shift_JIS");

        try {
            int status = client.executeMethod(method);
            if (status == 200) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            return false;
        } finally {
            method.releaseConnection();
        }
    }

    private String executeCommandWithResponse(PostMethod method) {
        HttpClient client = new HttpClient();
        client.getState().setCredentials(AuthScope.ANY, credentials);
        client.getParams().setParameter("http.connection.timeout",
                new Integer(3000));
        client.getParams().setParameter("http.socket.timeout",
                new Integer(5000));

        try {
            int status = client.executeMethod(method);
            if (status == 200) {
                return new String(method.getResponseBody(), "Shift_JIS");
            } else {
                return null;
            }
        } catch (IOException e) {
            return null;
        } finally {
            method.releaseConnection();
        }
    }
}
