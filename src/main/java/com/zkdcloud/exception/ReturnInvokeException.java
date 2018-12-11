package com.zkdcloud.exception;

/**
 * throw this exception will return object
 *
 * @author zk
 * @since 2018/12/11
 */
public class ReturnInvokeException extends InvokeException{

    private Object returnObj;

    public ReturnInvokeException(Object returnObj){
        super(returnObj.toString());
        this.returnObj = returnObj;
    }

    public Object getReturnObj() {
        return returnObj;
    }
}
