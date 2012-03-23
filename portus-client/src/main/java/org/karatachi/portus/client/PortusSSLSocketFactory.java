package org.karatachi.portus.client;

import java.security.KeyStore;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.karatachi.exception.IncompatiblePlatformException;

public class PortusSSLSocketFactory {
    public static void initialize(String keyStorePath, String trustStorePath) {
        HttpsURLConnection.setDefaultSSLSocketFactory(new PortusSSLSocketFactory(
                keyStorePath, trustStorePath).getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
    }

    private final SSLSocketFactory socketFactory;

    private PortusSSLSocketFactory(String keyStorePath, String trustStorePath) {
        try {
            char[] changeit = "changeit".toCharArray();

            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(getClass().getResourceAsStream(keyStorePath),
                    changeit);

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(keyStore, changeit);

            KeyStore trustStore = KeyStore.getInstance("JKS");
            trustStore.load(getClass().getResourceAsStream(trustStorePath),
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
