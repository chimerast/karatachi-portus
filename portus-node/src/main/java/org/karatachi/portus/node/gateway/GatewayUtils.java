package org.karatachi.portus.node.gateway;

import java.net.Inet4Address;
import java.net.InetAddress;

import org.karatachi.portus.node.AssemblyInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GatewayUtils {
    private static final Logger logger =
            LoggerFactory.getLogger(GatewayUtils.class);

    public static boolean setupNetGenesiss(Inet4Address gatewayAddr, int[] ports) {
        NetGenesis router = new NetGenesis(gatewayAddr.getHostAddress());
        if (router.authorize(AssemblyInfo.ROUTER_ADMIN_ID,
                AssemblyInfo.ROUTER_ADMIN_PASS)) {
            ;
        } else if (router.authorize("admin", "")) {
            router.setPassword(AssemblyInfo.ROUTER_ADMIN_ID,
                    AssemblyInfo.ROUTER_ADMIN_PASS);
        } else {
            return false;
        }

        boolean ret = true;
        InetAddress addr =
                WindowsNative.createTemporaryLocalAddress(gatewayAddr,
                        (byte) -2);
        if (addr == null) {
            return false;
        }

        ret &= router.setMtu(1438);
        ret &= router.updatePPPoE();

        ret &= router.removeIpMasquaradePPPoE();
        ret &= router.removeIpMasquaradeWan();

        for (int port : ports) {
            ret &=
                    router.addIpMasquaradePPPoE(addr.getHostAddress(),
                            Integer.toString(port));
            ret &=
                    router.addIpMasquaradeWan(addr.getHostAddress(),
                            Integer.toString(port));
        }

        ret &= router.updateIpMasquarade();

        if (ret) {
            router.save();
            logger.info("InternetGatewayDevice設定成功 再起動");
            return router.reboot();
        } else {
            return false;
        }
    }
}
