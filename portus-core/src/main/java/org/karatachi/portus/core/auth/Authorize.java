package org.karatachi.portus.core.auth;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.karatachi.portus.core.type.AccountRole;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.PACKAGE, ElementType.TYPE, ElementType.METHOD })
@Inherited
public @interface Authorize {
    AccountRole.Bit[] value() default {};
}
