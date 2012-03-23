package org.karatachi.portus.common.net;

import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.Test;
import org.karatachi.portus.common.crypto.EncryptionUtils;

public class AuthorizationInfoTest {
    @Test
    public void testReverse() throws Exception {
        String code = EncryptionUtils.customerIdToString(0);
        String url = "http://portus.karatachi.org/index.html";

        AuthorizationInfo info =
                new AuthorizationInfo(code, url, null, null, null);
        testReverse(info);
        info.expire = new Date().getTime();
        testReverse(info);
        info.username = "portus";
        testReverse(info);
        info.ipAddress = "0.0.0.0";
        testReverse(info);
    }

    private void testReverse(AuthorizationInfo info) throws Exception {
        String encrypted = AuthorizationInfo.encrypt(info);
        AuthorizationInfo decrypted = AuthorizationInfo.decrypt(encrypted);
        assertTrue(EqualsBuilder.reflectionEquals(info, decrypted));
    }
}
