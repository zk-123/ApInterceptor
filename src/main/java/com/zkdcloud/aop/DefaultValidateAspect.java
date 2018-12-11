package com.zkdcloud.aop;

import com.zkdcloud.exception.ProcessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;

/**
 * 默认异常处理
 *
 * @author zk
 * @since 2018/12/11
 */
public class DefaultValidateAspect extends AbstractValidateAspect {
    /**
     * static logger
     */
    private static Logger logger = LoggerFactory.getLogger(DefaultValidateAspect.class);
    
    public void renderThrowable(Throwable throwable) throws Throwable {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (servletRequestAttributes == null || servletRequestAttributes.getResponse() == null) {
            logger.warn("please set validate exception deal");
            throw throwable;
        }
        // default deal
        HttpServletResponse response = servletRequestAttributes.getResponse();
        Writer writer = null;

        try {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            writer = response.getWriter();
            writer.append(throwable.getMessage());
        } catch (IOException e) {
            logger.error("",e);
        } finally {
            try {
                writer.close();
                response.flushBuffer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
