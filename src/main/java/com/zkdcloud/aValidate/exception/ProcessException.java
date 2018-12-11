package com.zkdcloud.aValidate.exception;

/**
 * the top of validate Exception
 *
 * @author zk
 * @since 2018-01-23 15:18
 */
public class ProcessException extends Exception{
    private Throwable throwable;

    public ProcessException(String msg){
        super(msg);
    }

    public ProcessException(InvokeException throwable){
        super(throwable);
    }
}
