package com.zkdcloud.aValidate.annotation;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * annotation of real use
 *
 * {@see BeforeProcess}
 *
 * @author zk
 * @since 2018-01-22 10:14
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface Validate {
    Class<?> value();
    String method();
}
