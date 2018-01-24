package com.xdja.bean;

import java.io.Serializable;

/**
 * 返回的结果状态，只在接口场景并且返回json/application中使用
 *
 * @author zk
 * @date 2018-01-23 9:14
 */
public class ResultBean<T> implements Serializable{
    private static final long serialVersionUID = -3759707545985920736L;
    /**
     * 成功标识
     */
    public static final Integer RESPONSE_SUCCESS = 1;
    /**
     * 错误标识
     */
    public static final Integer RESPONSE_FAIL = 0;
    /**
     * 状态码
     */
    protected Integer code;
    /**
     * 提示消息
     */
    protected String message;
    /**
     * 返回实体
     */
    private T data;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    /**
     * 创建失败返回结果
     *
     * @param message message
     * @param data data
     * @return resultBean
     */
    public static <T>ResultBean<T> failResult(String message,T data){
        return create(RESPONSE_FAIL,message,data);
    }

    /**
     * 创建失败的返回结果
     *
     * @param message message
     * @param <T> T
     * @return resultBean of fail
     */
    public static <T>ResultBean<T> failResult(String message){
        return failResult(message,null);
    }

    /**
     * 创建成功返回结果
     *
     * @param message message
     * @param data data
     * @return resultBean
     */
    public static <T>ResultBean<T> successResult(String message,T data){
        return create(RESPONSE_SUCCESS,message,data);
    }
    /**
     * 创建成功返回结果
     *
     * @param message message
     */
    public static <T>ResultBean<T> successResult(String message){
        return successResult(message,null);
    }

    /**
     * 创建一个resultBean
     *
     * @param code code
     * @param message message
     * @param data data
     * @param <T> T
     * @return resultBean<T>
     */
    public static <T>ResultBean<T> create(Integer code,String message,T data){
        ResultBean<T> resultBean = new ResultBean<T>();
        resultBean.setCode(code);
        resultBean.setMessage(message);
        resultBean.setData(data);
        return resultBean;
    }
}
