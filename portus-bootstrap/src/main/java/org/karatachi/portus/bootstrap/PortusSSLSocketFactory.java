package org.karatachi.portus.bootstrap;

import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.karatachi.exception.IncompatiblePlatformException;

public class PortusSSLSocketFactory {
    private static final String SSL_TRUSTSTORE = "/truststore";
    private static final String SSL_KEYSTORE = "/keystore";
    private static final String SSL_KEY_PASS = "QMfOZHOoaYf2";

    private static final PortusSSLSocketFactory factory =
            new PortusSSLSocketFactory();

    public static PortusSSLSocketFactory getInstance() {
        return factory;
    }

    private final SSLSocketFactory socketFactory;

    public PortusSSLSocketFactory() {
        try {
            char[] changeit = "changeit".toCharArray();

            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(getClass().getResourceAsStream(SSL_KEYSTORE),
                    changeit);

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(keyStore, SSL_KEY_PASS.toCharArray());

            KeyStore trustStore = KeyStore.getInstance("JKS");
            trustStore.load(getClass().getResourceAsStream(SSL_TRUSTSTORE),
                    changeit);
            TrustManagerFactory tmf =
                    TrustManagerFactory.getInstance("SunX509");
            tmf.init(trustStore);

            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            socketFactory = ctx.getSocketFactory();
        } catch (Exception e) {
            throw new IncompatiblePlatformException("Fail to load SSL keys.", e);
        }
    }

    public SSLSocketFactory getSocketFactory() {
        return socketFactory;
    }
}
