package com.xdja.util;

import com.xdja.bean.ResultBean;
import com.xdja.exception.InvokeException;

/**
 * validateUtils
 * 用于control，service层校验，抛出特定异常
 *
 * @author zk
 * @date 2018-01-23 12:05
 */
public class ValidateUtils {
    /**
     * 断言条件
     *
     * @param condition condition
     * @param message message
     */
    public static void assertCondition(Boolean condition,String message) throws InvokeException {
        if(!condition){
            throw new InvokeException(message);
        }
    }

    /**
     * 断言条件
     *
     * @param condition condition
     * @param resultBean httpBean
     * @throws InvokeException
     */
    public static void assertCondition(Boolean condition,ResultBean resultBean)throws InvokeException{
        if(!condition){
            throw new InvokeException(resultBean);
        }
    }
}

