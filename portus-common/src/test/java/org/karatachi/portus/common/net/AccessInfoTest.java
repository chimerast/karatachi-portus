package org.karatachi.portus.common.net;

import static org.junit.Assert.assertTrue;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.Test;

public class AccessInfoTest {
    @Test
    public void testReverse() {
        AccessInfo info =
                new AccessInfo(0, "192.168.0.1", "index.html", 2400, false,
                        false);

        String encrypted = AccessInfo.encrypt(info);

        AccessInfo decrypted = AccessInfo.decrypt(encrypted);

        assertTrue(EqualsBuilder.reflectionEquals(info, decrypted));

        decrypted =
                AccessInfo.decrypt("PD_ZWcOa3K9rV7naQx_AmsZH9CuV8dlPBG7k.DQ.maiu8lgGuKKYVay.SAUck0aO");
        decrypted.ipAddress = "0.0.0.0";
        decrypted.expire =
                System.currentTimeMillis() + 100 * 24 * 60 * 60 * 1000;
        System.out.println(decrypted);
    }
}
