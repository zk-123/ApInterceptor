package com.xdja.annotation;

import java.lang.annotation.*;

/**
 * after Process
 * todo 待考虑
 *
 * @author zk
 * @date 2018-01-22 10:16
 */
@Inherited
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AfterProcess {
}
