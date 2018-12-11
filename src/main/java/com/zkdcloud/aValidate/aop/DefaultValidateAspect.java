package com.zkdcloud.aValidate.aop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 默认异常处理
 *
 * @author zk
 * @since 2018/12/11
 */
public class DefaultValidateAspect extends AbstractValidateAspect {
    /**
     * static logger
     */
    private static Logger logger = LoggerFactory.getLogger(DefaultValidateAspect.class);
    
    public Object renderThrowable(Throwable throwable) throws Throwable {
        logger.error("请设置默认异常处理");
        return throwable.getMessage();
    }
}
