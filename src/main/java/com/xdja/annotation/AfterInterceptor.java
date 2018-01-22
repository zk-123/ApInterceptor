package com.xdja.annotation;

/**
 * describe
 *
 * @author zk
 * @date 2018-01-22 10:17
 */
public @interface AfterInterceptor {
    Class<?> value();
    String method();
}
