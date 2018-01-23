package com.xdja.exception;

import com.xdja.bean.ResultBean;

/**
 * 通知异常
 *
 * @author zk
 * @date 2018-01-23 11:18
 */
public class AdviceException extends ProcessException{

    public AdviceException(ResultBean resultBean) {
        super(resultBean);
    }
}
