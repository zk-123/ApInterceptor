package com.xdja.annotation;

import java.lang.annotation.*;

/**
 * before Process
 *
 * @author zk
 * @date 2018-01-22 10:13
 */
@Inherited
@Documented
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface BeforeProcess {
    BeforeInterceptor[] value();
}
