package com.zkdcloud.annotation;

import com.zkdcloud.advice.DoNothingAdvice;
import com.zkdcloud.advice.HttpAdvice;
import com.zkdcloud.validate.DoNothingValidate;
import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.annotation.*;

/**
 * before Process
 *
 * @author zk
 * @date 2018-01-22 10:13
 */
@Inherited
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ResponseBody
public @interface BeforeProcess {
    Validate[] validate() default @Validate(value = DoNothingValidate.class,method = "doNothing");
    Class<? extends HttpAdvice>[] advice() default DoNothingAdvice.class;
}
