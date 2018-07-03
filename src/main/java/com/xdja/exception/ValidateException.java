package com.xdja.exception;

import com.xdja.bean.ResultBean;

/**
 * 校验异常
 *
 * @author zk
 * @date 2018-01-22 16:53
 */
public class ValidateException extends ProcessException{

    public ValidateException(ResultBean resultBean) {
        super(resultBean);
    }
}
