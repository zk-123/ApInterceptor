package com.xdja.exception;

import com.xdja.bean.ResultBean;

/**
 * describe
 *
 * @author zk
 * @date 2018-01-23 15:18
 */
public class ProcessException extends Exception{
    /**
     * 结果bean
     */
    protected ResultBean resultBean;

    public ProcessException(ResultBean resultBean){

    }
    public ResultBean getResultBean() {
        return resultBean;
    }

    public void setResultBean(ResultBean resultBean) {
        this.resultBean = resultBean;
    }
}
