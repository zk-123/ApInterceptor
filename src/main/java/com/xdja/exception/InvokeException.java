package com.xdja.exception;

import com.xdja.bean.ResultBean;

/**
 * 调用错误异常
 *
 * @author zk
 * @date 2018-01-23 11:39
 */
public class InvokeException extends Exception{
    /**
     * 结果bean
     */
    private ResultBean resultBean;

    public InvokeException(ResultBean resultBean){
        this.resultBean = resultBean;
    }

    public InvokeException(String message){
        super(message);
        resultBean = ResultBean.failResult(message);
    }

    public ResultBean getResultBean() {
        return resultBean;
    }

    public void setResultBean(ResultBean resultBean) {
        this.resultBean = resultBean;
    }
}
