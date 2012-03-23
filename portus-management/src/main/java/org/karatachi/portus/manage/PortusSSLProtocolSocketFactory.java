package org.karatachi.portus.manage;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;
import org.karatachi.exception.IncompatiblePlatformException;

public class PortusSSLProtocolSocketFactory implements
        SecureProtocolSocketFactory {
    private static final String SSL_TRUSTSTORE = "/truststore";
    private static final String SSL_KEYSTORE = "/keystore";
    private static final String SSL_KEY_PASS = "nLEj0NE14NM9";

    private static final PortusSSLProtocolSocketFactory factory =
            new PortusSSLProtocolSocketFactory();

    public static PortusSSLProtocolSocketFactory getInstance() {
        return factory;
    }

    private final SSLSocketFactory socketFactory;
    private final SSLServerSocketFactory serverSocketFactory;

    public PortusSSLProtocolSocketFactory() {
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
            serverSocketFactory = ctx.getServerSocketFactory();
        } catch (Exception e) {
            throw new IncompatiblePlatformException("Fail to load SSL keys.", e);
        }
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localAddress,
            int localPort) throws IOException, UnknownHostException {
        return socketFactory.createSocket(host, port, localAddress, localPort);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localAddress,
            int localPort, HttpConnectionParams params) throws IOException,
            UnknownHostException, ConnectTimeoutException {
        if (params == null) {
            throw new IllegalArgumentException("Parameters may not be null");
        }

        int timeout = params.getConnectionTimeout();
        if (timeout == 0) {
            return createSocket(host, port, localAddress, localPort);
        } else {
            Socket socket = socketFactory.createSocket();
            SocketAddress localaddr =
                    new InetSocketAddress(localAddress, localPort);
            SocketAddress remoteaddr = new InetSocketAddress(host, port);
            socket.bind(localaddr);
            socket.connect(remoteaddr, timeout);
            return socket;
        }
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException,
            UnknownHostException {
        return socketFactory.createSocket(host, port);
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port,
            boolean autoClose) throws IOException, UnknownHostException {
        return socketFactory.createSocket(socket, host, port, autoClose);
    }

    public ServerSocket createServerSocket(int port) throws IOException {
        return serverSocketFactory.createServerSocket(port);
    }

    public ServerSocket createServerSocket(int port, boolean clientAuth)
            throws IOException {
        SSLServerSocket socket =
                (SSLServerSocket) serverSocketFactory.createServerSocket(port);
        socket.setNeedClientAuth(clientAuth);
        return socket;
    }
}
