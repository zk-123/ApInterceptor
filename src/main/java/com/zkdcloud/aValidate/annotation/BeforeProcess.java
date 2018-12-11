package com.zkdcloud.aValidate.annotation;

import com.zkdcloud.aValidate.advice.HttpAdvice;

import java.lang.annotation.*;

/**
 * before Process
 *
 * <code>
 *     {@link BeforeProcess}(validate={{@link Validate}(value = DoNothingValidate.class,method = "doNothing")})
 *     public void buyBook(){
 *     }
 * </code>
 *
 * @author zk
 * @since 2018-01-22 10:13
 */
@Inherited
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface BeforeProcess {
    Validate[] validate() default {};
    Class<? extends HttpAdvice>[] advice() default {};
}
