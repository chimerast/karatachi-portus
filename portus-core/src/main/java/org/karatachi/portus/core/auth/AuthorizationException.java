package org.karatachi.portus.core.auth;

import java.net.HttpURLConnection;

import org.karatachi.portus.core.PortusRuntimeException;

/**
 * 認証失敗例外。AuthorizationInterceptorによるメソッドの認証が失敗した際に生成される。
 * 
 * @author chimera
 */
public class AuthorizationException extends PortusRuntimeException {
    private static final long serialVersionUID = 1L;

    public AuthorizationException() {
        super("許可されない操作です。", HttpURLConnection.HTTP_FORBIDDEN);
    }
}
