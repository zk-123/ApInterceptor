package com.xdja.advice;

import com.xdja.exception.AdviceException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 通知类型(前置通知,后置通知)
 *
 * @author zk
 * @date 2018-01-23 10:57
 */
public interface HttpAdvice {
    /**
     * 执行通知类型
     *
     * @param request request
     * @param response response
     * @param transportData 传输参数
     */
    void doAdvice(HttpServletRequest request, HttpServletResponse response,Map<String, Object> transportData) throws AdviceException;
}
