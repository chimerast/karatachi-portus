package org.karatachi.portus.core.auth;

import org.aopalliance.intercept.MethodInvocation;
import org.karatachi.annotation.AnnotationUtils;
import org.karatachi.portus.core.dto.AccountDto;
import org.karatachi.portus.core.type.AccountRole;
import org.seasar.framework.aop.interceptors.AbstractInterceptor;
import org.seasar.framework.container.annotation.tiger.Binding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * メソッド認証を行うSeasar2用Interceptor。AuthenticatedClassを継承したクラスの各メソッドの認証を行う。
 * AuthorizeMethodアノテーションにより指定されたロールとAuthenticatedClassに保持された
 * 承認情報の比較を行いメソッド毎の認証を行う。
 * 
 * @author chimera
 */
public class AuthorizationInterceptor extends AbstractInterceptor {
    private static final long serialVersionUID = 1L;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Binding
    private AccountDto accountDto;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Authorize annotation =
                invocation.getMethod().getAnnotation(Authorize.class);

        if (annotation == null) {
            annotation =
                    AnnotationUtils.getInterfaceAnnotation(
                            invocation.getMethod(), Authorize.class);
        }

        if (annotation != null) {
            for (AccountRole.Bit bit : annotation.value()) {
                if (!accountDto.hasAccountRole(bit)) {
                    logger.info("認証失敗: " + accountDto);
                    throw new AuthorizationException();
                }
            }
        }
        return invocation.proceed();
    }
}
