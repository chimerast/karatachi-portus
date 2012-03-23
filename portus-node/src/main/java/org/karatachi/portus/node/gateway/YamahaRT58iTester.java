package org.karatachi.portus.node.gateway;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class YamahaRT58iTester {
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        byte[] addr = { (byte) 192, (byte) 168, 100, 1 };
        YamahaRT58iController controller = null;
        try {
            controller =
                    new YamahaRT58iController(InetAddress.getByAddress(addr),
                            23, "aaa", "");
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String line;
        //line = controller.showConfig();
        //System.out.println(line);
        String[] msgs =
                {
                        //      "ip lan1 address 192.168.50.1/24",
                        "no dhcp service server",
                        "no dhcp server rfc2131 compliant except remain-silent",
                        "no dhcp scope 1 192.168.100.2-192.168.100.191/24",
                        "no dns private name setup.netvolante.jp",
                        "no analog supplementary-service pseudo call-waiting",
                        "no analog extension dial prefix line",
                        "no analog extension dial prefix sip prefix=\"9#\"",
                        "ip routing on",
                        //"pppoe use wan",
                        "ip icmp echo-reply send off",
                        "nat descriptor type 1 masquerade",
                        "nat descriptor address outer 1 ipcp",
                        "nat descriptor address inner 1 192.168.100.2-192.168.100.10",
                        // 死活監視などのパケットの優先順位を上げる？
                        "queue class filter 1 4 ip * * 6 5000 5000",

                        "pp select 1",
                        "pppoe auto connect on",
                        "pppoe tcp mss limit auto",
                        "ppp ipcp msext on",
                        "dns server pp 1",
                        "ip pp nat descriptor 1",
                        // パケット送信キューをpriority queueにする
                        "queue pp type priority", "no pp select 1",
                        "pptp service on", "pp select 2",
                        "pp bind tunnel1-tunnel4", "pptp hostname hogehoge",
                        "pptp service type server",
                        "pp auth request mschap-v2",
                        "pp auth accept mschap mschap-v2", "no pp select 2", ""

                };
        line = controller.administratorCommands(msgs);
        System.out.println(line);
    }
}
