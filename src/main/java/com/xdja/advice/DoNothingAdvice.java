package com.xdja.advice;

import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * advice example
 *
 * @author zk
 * @date 2018-01-23 11:10
 */
@Component
public class DoNothingAdvice implements HttpAdvice{
    public void doAdvice(HttpServletRequest request, HttpServletResponse response, Map<String, Object> transportData) {
    }
}
