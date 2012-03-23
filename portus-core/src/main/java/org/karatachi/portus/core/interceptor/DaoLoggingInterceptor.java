package org.karatachi.portus.core.interceptor;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.StringUtils;
import org.seasar.framework.aop.S2MethodInvocation;
import org.seasar.framework.aop.interceptors.AbstractInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DaoLoggingInterceptor extends AbstractInterceptor {
    private static final long serialVersionUID = 1L;

    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        long start = System.currentTimeMillis();
        Object ret = invocation.proceed();
        long end = System.currentTimeMillis();

        long elapsed = end - start;
        if (logger.isDebugEnabled()) {
            Class<?> clazz = ((S2MethodInvocation) invocation).getTargetClass();
            String info =
                    String.format("%s#%s(%s)", clazz.getSimpleName(),
                            invocation.getMethod().getName(), StringUtils.join(
                                    invocation.getArguments(), ", "));
            logger.debug("{} ms: {}", elapsed, info);
        }

        return ret;
    }
}
