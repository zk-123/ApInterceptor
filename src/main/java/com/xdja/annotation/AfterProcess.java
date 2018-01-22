package com.xdja.annotation;

/**
 * after Process
 *
 * @author zk
 * @date 2018-01-22 10:16
 */
public @interface AfterProcess {
    AfterInterceptor[] value();
}
