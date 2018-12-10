package com.zkdcloud.aop;

import com.zkdcloud.advice.HttpAdvice;
import com.zkdcloud.annotation.BeforeProcess;
import com.zkdcloud.annotation.Validate;
import com.zkdcloud.constant.Constant;
import com.zkdcloud.exception.AdviceException;
import com.zkdcloud.exception.InvokeException;
import com.zkdcloud.exception.ProcessException;
import com.zkdcloud.exception.ValidateException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * main Aspect
 * (虽然调用的是aop，与interceptor毫不相关，但是对外表现的行为就是一个拦截器，所以在这里引用说法)
 *
 * @author zk
 * @since 2018-01-22 10:10
 */
@Aspect
@Component
public class AroundAspect implements Ordered {
    private static Logger logger = LoggerFactory.getLogger(AroundAspect.class);

    @Autowired
    private WebApplicationContext webApplicationContext;

    /**
     * transport Data (interceptor传递链中，传递的值，包含目标方法原始参数列表)
     */
    private Map<String, Object> transportData = new ConcurrentHashMap<String, Object>();

    @Pointcut(value = "@annotation(com.zkdcloud.annotation.BeforeProcess)")
    public void aValidate() {
    }

    /**
     * before validate and advice
     *
     * @param joinPoint joinPoint
     */
    @Before("aValidate()")
    public void doBeforeValidate(JoinPoint joinPoint) {
        try {
            doBeforeProcess(joinPoint);
        } catch (ProcessException e) {
            renderThrowable(e);
        }
    }


    /**
     * 执行BeforeProcess(暂时分为 before Advice 和 before validate)
     *
     * @param joinPoint joinPoint
     * @throws ProcessException exception
     */
    public void doBeforeProcess(JoinPoint joinPoint) throws ProcessException {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        //存储被调用方法参数
        String[] paramNames = methodSignature.getParameterNames();
        Object[] paramsValues = joinPoint.getArgs();
        setTransportData(paramNames, paramsValues);
        //获取注解并调用
        Method method = methodSignature.getMethod();
        BeforeProcess beforeProcess = method.getAnnotation(BeforeProcess.class);

        //如果存在beforeProcess，则继续执行操作
        if (beforeProcess != null) {
            doBeforeAdvice(beforeProcess);
            doBeforeValidate(beforeProcess);
        }
    }

    /**
     * 执行前置通知
     *
     * @param beforeProcess beforeProcess
     * @throws AdviceException adviceException
     */
    public void doBeforeAdvice(BeforeProcess beforeProcess) throws AdviceException {
        Class<? extends HttpAdvice>[] beforeAdvices = beforeProcess.advice();
        for (Class<? extends HttpAdvice> adviceClass : beforeAdvices) {
            invokedSpecialMethod(adviceClass, "doAdvice");
        }
    }

    /**
     * 执行前置校验
     *
     * @param beforeProcess beforeProcess
     * @throws ValidateException validateException
     */
    public void doBeforeValidate(BeforeProcess beforeProcess) throws ValidateException {
        Validate[] beforeValidates = beforeProcess.validate();
        for (Validate beforeValidate : beforeValidates) {
            //被调用的before类
            Class<?> invokedClass = beforeValidate.value();
            //被调用的before类中指定方法
            String invokedMethodName = beforeValidate.method();

            //执行调用方法
            try {
                invokedSpecialMethod(invokedClass, invokedMethodName);
            } catch (Exception e) {
                throw new ValidateException(e.getMessage());
            }
        }
    }

    /**
     * 调用指定方法
     *
     * @param clazz      clazz
     * @param methodName methodName
     */
    public void invokedSpecialMethod(Class<?> clazz, String methodName) throws InvokeException {
        try {
            //may be is null
            Object targetObj = webApplicationContext.getBean(clazz);
            if (targetObj != null) {
                Method[] methods = clazz.getDeclaredMethods();
                for (Method method : methods) {
                    if (method.getName().equalsIgnoreCase(methodName)) {
                        Object[] paramValues = getInvokedSpecialMethodParams(method);
                        method.invoke(targetObj, paramValues);
                        return;
                    }
                }
            } else {
                logger.error("Spring Bean中找不到" + clazz + "对象");
                return;
            }
        } catch (Exception e) {
            //如果是用户主动抛，则直接抛出
            if (e instanceof InvocationTargetException && ((InvocationTargetException) e).getTargetException() instanceof InvokeException) {
                throw ((InvokeException) ((InvocationTargetException) e).getTargetException());
            } else {
                logger.error("调用" + clazz + "#" + methodName + "出错", e);
                throw new InvokeException(Constant.SYSTEM_ERRROR);
            }
        }
        logger.warn("未找到该" + clazz + "中的" + methodName + "方法");
    }

    /**
     * 获取拦截器方法上，参数值(若无，则为null)
     *
     * @param method method
     * @return Object[]
     */
    public Object[] getInvokedSpecialMethodParams(Method method) {
        List result = new ArrayList();
        //spring discover method parameter names
        LocalVariableTableParameterNameDiscoverer discovererNames =
                new LocalVariableTableParameterNameDiscoverer();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();

        //get parameter Object
        String[] paramNames = discovererNames.getParameterNames(method);
        for (int i = 0; i < paramNames.length; i++) {
            //原参数
            if (transportData.containsKey(paramNames[i])) {
                result.add(transportData.get(paramNames[i]));
            } else if (method.getParameterTypes()[i].equals(HttpServletRequest.class)) {
                // request object
                result.add(request);
            } else if (method.getParameterTypes()[i].equals(HttpServletResponse.class)) {
                // response object
                result.add(response);
            } else if (method.getParameterTypes()[i].equals(Map.class)) {
                // transportData (如果找不到已经在目标方法中命名的map,则默认是transportData)
                result.add(transportData);
            } else {
                logger.warn(method.getDeclaringClass() + "#" + method.getName() + "参数未找到，设为null");
                result.add(null);
            }
        }
        return result.toArray();
    }

    /**
     * 渲染异常（JSON类型）
     *
     * @param throwable resultBean
     */
    public void renderThrowable(Throwable throwable) {
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        Writer writer = null;

        try {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            writer = response.getWriter();
            writer.append(throwable.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
                response.flushBuffer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 设置transportData 中的值
     *
     * @param paramNames   参数名
     * @param paramsValues 参数值
     */
    public void setTransportData(String[] paramNames, Object[] paramsValues) {
        try {
            for (int i = 0; i < paramNames.length; i++) {
                setTransportData(paramNames[i], paramsValues[i]);
            }
        } catch (Exception e) {
            logger.error("参数名列表长度和参数值长度不符", e);
        }
    }


    /**
     * 设置transportData 中的值
     *
     * @param key   key
     * @param value value
     */
    public void setTransportData(String key, Object value) {
        transportData.put(key, value);
    }

    public int getOrder() {
        return Integer.MAX_VALUE;
    }
}
