package com.zkdcloud.aop;

import com.zkdcloud.advice.HttpAdvice;
import com.zkdcloud.annotation.BeforeProcess;
import com.zkdcloud.annotation.Validate;
import com.zkdcloud.exception.ProcessException;
import com.zkdcloud.exception.ReturnInvokeException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.Ordered;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * validate aspect
 *
 * @author zk
 * @since 2018-01-22 10:10
 */
public abstract class AbstractValidateAspect implements Ordered {
    private static Logger logger = LoggerFactory.getLogger(AbstractValidateAspect.class);

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * transport Data (interceptor传递链中，传递的值，包含目标方法原始参数列表)
     */
    private Map<String, Object> transportData = new HashMap<String, Object>();

    @Pointcut(value = "@annotation(com.zkdcloud.annotation.BeforeProcess)")
    public void aValidate() {}

    /**
     * before validate and advice
     *
     * @param joinPoint joinPoint
     */
    @Around("aValidate()")
    public Object doBeforeValidate(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            doBeforeProcess(joinPoint);
        } catch (Exception e) {
            // the throw of method invocation
            if (e instanceof InvocationTargetException) {
                e = (Exception) ((InvocationTargetException) e).getTargetException();
                if (e instanceof ReturnInvokeException) {
                    return ((ReturnInvokeException) e).getReturnObj();
                }
            }

            return renderThrowable(e);
        }

        return joinPoint.proceed();
    }

    /**
     * 渲染异常
     *
     * @param throwable resultBean
     */
    public abstract Object renderThrowable(Throwable throwable) throws Throwable;

    /**
     * 执行BeforeProcess(暂时分为 before Advice 和 before validate)
     *
     * @param joinPoint joinPoint
     * @throws ProcessException exception
     */
    @SuppressWarnings("unchecked")
    public void doBeforeProcess(JoinPoint joinPoint) throws Exception {
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
     * @param beforeProcess 执行前置通知
     * @throws Exception exception
     */
    public void doBeforeAdvice(BeforeProcess beforeProcess) throws Exception {
        Class<? extends HttpAdvice>[] beforeAdvices = beforeProcess.advice();
        for (Class<? extends HttpAdvice> adviceClass : beforeAdvices) {
            invokedSpecialMethod(adviceClass, "doAdvice");
        }
    }

    /**
     * 执行前置校验
     *
     * @param beforeProcess beforeProcess
     * @throws Exception validateException
     */
    public void doBeforeValidate(BeforeProcess beforeProcess) throws Exception {
        Validate[] beforeValidates = beforeProcess.validate();
        for (Validate beforeValidate : beforeValidates) {
            //被调用的before类
            Class<?> invokedClass = beforeValidate.value();
            //被调用的before类中指定方法
            String invokedMethodName = beforeValidate.method();

            //执行调用方法
            invokedSpecialMethod(invokedClass, invokedMethodName);
        }
    }

    /**
     * 调用指定方法
     *
     * @param clazz      clazz
     * @param methodName methodName
     */
    public void invokedSpecialMethod(Class<?> clazz, String methodName) throws Exception {
        //may be is null
        Object targetObj = applicationContext.getBean(clazz);
        if (targetObj != null) {
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().equalsIgnoreCase(methodName)) {
                    Object[] paramValues = getInvokedSpecialMethodParams(method);
                    try {
                        method.invoke(targetObj, paramValues);
                    } catch (Exception e) {
                        throw e;
                    }
                    return;
                }
            }
        } else {
            logger.warn("Spring Bean中找不到" + clazz + "对象");
        }
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
        LocalVariableTableParameterNameDiscoverer discovererNames = new LocalVariableTableParameterNameDiscoverer();
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes == null ? null : attributes.getRequest();
        HttpServletResponse response = attributes == null ? null : attributes.getResponse();
        if (request == null) {
            logger.warn("validate request is null , please autowired request");
        }
        if (response == null) {
            logger.warn("validate response is null , please autowired response");
        }

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
     * 设置transportData 中的值
     *
     * @param paramNames   参数名
     * @param paramsValues 参数值
     */
    public void setTransportData(String[] paramNames, Object[] paramsValues) {
        int valuesLength = paramsValues == null ? 0 : paramsValues.length;
        for (int i = 0; i < paramNames.length; i++) {
            if(i < valuesLength){
                setTransportData(paramNames[i], paramsValues[i]);
            } else {
                setTransportData(paramNames[i], null);
            }
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
