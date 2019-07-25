package com.wpj.spring.boot.rocketmq.starter.processor.proxy;

/**
 * @author wangpejian
 * @date 19-3-29 下午5:17
 */
public interface MethodHandler {

    Object invoke(Object[] argv) throws Throwable;

}
