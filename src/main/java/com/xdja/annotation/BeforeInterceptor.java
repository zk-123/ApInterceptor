package com.xdja.annotation;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * describe
 *
 * @author zk
 * @date 2018-01-22 10:14
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface BeforeInterceptor {
    Class<?> value();
    String method();
}
