package com.wpj.spring.boot.rocketmq.starter.processor.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * 消息发送代理类
 *
 * @author wangpejian
 * @date 19-3-29 上午11:12
 */
public class ChannelProxyHandler implements InvocationHandler {

    private Map<Method, MethodHandler> methodToHandler;

    ChannelProxyHandler(Map<Method, MethodHandler> methodToHandler) {
        this.methodToHandler = methodToHandler;
    }

    /**
     * 代理发送MQ消息
     *
     * @param proxy
     * @param method
     * @param args
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return methodToHandler.get(method).invoke(args);
    }
}
