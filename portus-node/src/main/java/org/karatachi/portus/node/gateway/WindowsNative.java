package org.karatachi.portus.node.gateway;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import org.karatachi.jni.win32.NetworkSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WindowsNative {
    private static final Logger logger =
            LoggerFactory.getLogger(WindowsNative.class);

    /**
     * 指定したネットワークに接続されたインターフェースに関連づけられたIPアドレスを取得する
     * 
     * @param network
     *            ネットワークのIPアドレス（第４オクテットは無視）
     * @return networkに接続するインターフェースのIPアドレス
     */
    public static InetAddress getLocalAddress(Inet4Address network) {
        if (network == null) {
            return null;
        }

        byte[] gateway = network.getAddress();
        try {
            Enumeration<NetworkInterface> ifaces =
                    NetworkInterface.getNetworkInterfaces();
            while (ifaces.hasMoreElements()) {
                NetworkInterface iface = ifaces.nextElement();
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (!(address instanceof Inet4Address)) {
                        continue;
                    }

                    byte[] local = address.getAddress();
                    if (local[0] == gateway[0] && local[1] == gateway[1]
                            && local[2] == gateway[2]) {
                        return address;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("ローカルアドレス取得失敗", e);
        }
        return null;
    }

    /**
     * 指定したネットワークに接続するインターフェースに新しいIPアドレスを割り当てる。
     * 
     * @param netaddr
     *            ネットワークのIPアドレス（第４オクテットは無視）
     * @param least
     *            作成したいIPアドレスの第４オクテット
     * @return 新しく割り当てられたIPアドレス
     */
    public static InetAddress createTemporaryLocalAddress(Inet4Address netaddr,
            byte least) {
        if (netaddr == null) {
            return null;
        }

        byte[] network = netaddr.getAddress();
        try {
            Enumeration<NetworkInterface> ifaces =
                    NetworkInterface.getNetworkInterfaces();
            while (ifaces.hasMoreElements()) {
                NetworkInterface iface = ifaces.nextElement();
                Enumeration<InetAddress> addrs = iface.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress addr = addrs.nextElement();
                    if (!(addr instanceof Inet4Address)) {
                        continue;
                    }

                    byte[] local = addr.getAddress();
                    if (local[0] == network[0] && local[1] == network[1]
                            && local[2] == network[2]) {
                        local[3] = least;
                        Inet4Address newaddr =
                                (Inet4Address) InetAddress.getByAddress(local);
                        if (NetworkSetting.addIPAddress((Inet4Address) addr,
                                newaddr)) {
                            return newaddr;
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("ローカルアドレス取得失敗", e);
        }
        return null;
    }

    /**
     * WindowsファイアーウォールでJava.exeを許可する
     * 
     * @return
     */
    public static boolean setupWindowsFirewall() {
        try {
            String command =
                    "netsh firewall add allowedprogram C:\\Java\\bin\\java.exe Java ENABLE";
            Process process = Runtime.getRuntime().exec(command);
            if (process.waitFor() == 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.error("ファイアウォールの設定に失敗", e);
            return false;
        }
    }

    public static boolean stopIIS() {
        try {
            String command = "iisreset /stop";
            Process process = Runtime.getRuntime().exec(command);
            if (process.waitFor() == 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.error("IISの終了に失敗", e);
            return false;
        }
    }
}
