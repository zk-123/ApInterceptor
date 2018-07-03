package com.xdja.annotation;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 校验()
 *
 * @author zk
 * @date 2018-01-22 10:14
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface Validate {
    Class<?> value();
    String method();
}
