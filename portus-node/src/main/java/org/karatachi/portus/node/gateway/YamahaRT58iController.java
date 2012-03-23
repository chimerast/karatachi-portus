package org.karatachi.portus.node.gateway;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class YamahaRT58iController {
    String userPassword;
    String adminPassword;
    InetAddress routerAddress;
    int routerPort;
    boolean telnetSession = false;
    private Socket socket;

    private byte[] commandSuppressGoAhead = { (byte) 0xff, (byte) 0xfd, 0x03 };
    private byte[] commandWontEcho = { (byte) 0xff, (byte) 0xfc, 0x01 };
    private byte[] commandWontNAWS = { (byte) 0xff, (byte) 0xfc, (byte) 0x1f };
    private byte[] commandDontStatus = { (byte) 0xff, (byte) 0xfe, 0x05 };
    private byte[] commandWontRemoteFlowControl =
            { (byte) 0xff, (byte) 0xfc, 0x21 };

    public Socket getSocket() {
        if (socket == null) {
            socket = new Socket();
        }
        if (!socket.isConnected()) {
            try {
                socket.connect(new InetSocketAddress(routerAddress, routerPort));
                OutputStream os = socket.getOutputStream();
                PrintWriter pw = new PrintWriter(os);
                os.write(commandSuppressGoAhead);
                os.write(commandWontEcho);
                os.write(commandWontNAWS);
                os.write(commandDontStatus);
                os.write(commandWontRemoteFlowControl);
                os.flush();
                Thread.sleep(200);
                InputStream is = socket.getInputStream();
                int len = is.available();
                byte buffer[] = new byte[4096];
                is.read(buffer);
                String line = new String(buffer, 0, len, "sjis");
                // Yamahaルーターログイン時に表示されるメッセージをConsoleに出力
                // 必要がなければ削除
                // System.out.print(line);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return socket;

    }

    public YamahaRT58iController() {
    }

    public void authorize() {
        Socket s = getSocket();
    }

    private String userLogin() {
        String msg[] = { userPassword };
        String ret = sendMessage(msg);
        return ret;
    }

    private String administratorLogin() {
        String msg[] = { "administrator", adminPassword };
        String ret = sendMessage(msg);
        return ret;
    }

    private String exitAdministrator() {
        String msg[] = { "exit" };
        String msgN[] = { "N" };
        String ret = sendMessage(msg);
        if (ret.indexOf("(Y/N)") != -1) {
            sendMessage(msgN);
        }
        return ret;
    }

    private String exitUser() {
        String msg[] = { "exit" };
        String ret = sendMessage(msg);
        return ret;
    }

    public String showConfig() {
        userLogin();
        String msg[] = { "show config" };
        String ret = sendMessage(msg);
        exitUser();
        return ret;
    }

    private String sendMessage(byte[][] msgs) {
        Socket sock = getSocket();
        StringBuffer sbReturn = new StringBuffer();

        InputStream is = null;
        OutputStream os = null;
        try {
            is = sock.getInputStream();
            os = sock.getOutputStream();
            for (byte[] msg : msgs) {
                os.write(msg);
                os.flush();
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                sbReturn.append(line + "\r\n");
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return sbReturn.toString();
    }

    private String sendMessage(String[] msgs) {
        Socket sock = getSocket();
        StringBuffer sbReturn = new StringBuffer();
        try {
            OutputStream os = sock.getOutputStream();
            InputStream is = sock.getInputStream();
            PrintWriter pw = new PrintWriter(os);
            for (String msg : msgs) {
                pw.print(msg + "\r\n");
                pw.flush();
                Thread.sleep(200);
                int len = is.available();
                byte buffer[] = new byte[4096];
                is.read(buffer);
                String line = new String(buffer, 0, len, "sjis");
                sbReturn.append(line);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return sbReturn.toString();
    }

    public String loginPasswordEncrypted(String newPassword) {
        userLogin();
        administratorLogin();
        String msg[] =
                { "login password encrypted", userPassword, newPassword,
                        newPassword };
        String ret = sendMessage(msg);
        // TODO:必要なら正しくパスワードが変更できたかどうか確認
        userPassword = newPassword;

        exitAdministrator();
        exitUser();
        return ret;
    }

    public String userCommands(String[] msgs) {
        String ret;
        userLogin();
        ret = sendMessage(msgs);
        exitUser();
        return ret;
    }

    public String administratorCommands(String[] msgs) {
        String ret;
        userLogin();
        administratorLogin();
        ret = sendMessage(msgs);
        exitAdministrator();
        exitUser();
        return ret;
    }

    public YamahaRT58iController(InetAddress address, int port,
            String userPassword, String adminPassword) {
        this.routerAddress = address;
        this.routerPort = port;
        this.userPassword = userPassword;
        this.adminPassword = adminPassword;
    }

    public boolean setLan1IPAddress(InetAddress address) {
        return false;
    }

    public boolean setIpRouting(boolean flag) {
        return false;
    }
}
