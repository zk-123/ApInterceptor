package com.xdja.exception;

/**
 * 调用链中断
 *
 * @author zk
 * @date 2018-01-22 16:53
 */
public class InterceptorBreakException extends Exception{
    public InterceptorBreakException(String message){
        super(message);
    }
}
