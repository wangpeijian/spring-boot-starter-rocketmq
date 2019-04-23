package com.wpj.spring.boot.rocketmq.starter.processor.proxy;

import com.wpj.spring.boot.rocketmq.starter.annotation.MessageSender;
import com.wpj.spring.boot.rocketmq.starter.repertory.ChannelRepertory;
import lombok.Data;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @auther wangpejian
 * @date 19-3-29 下午2:37
 */
@Data
public class RocketClientFactoryBean implements FactoryBean<Object>, InitializingBean, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private Class<?> type;

    private ChannelRepertory channelRepertory;

    @Override
    public Object getObject() {
        Map<Method, MethodHandler> methodToHandler = getMethodHandlers();
        InvocationHandler invocationHandler = new ChannelProxyHandler(methodToHandler);
        return Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, invocationHandler);
    }

    @Override
    public Class<?> getObjectType() {
        return this.type;
    }

    @Override
    public void afterPropertiesSet() {

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 根据接口定义的方法，生成调用对象
     *
     * @return
     */
    private Map<Method, MethodHandler> getMethodHandlers() {
        Map<Method, MethodHandler> methodToHandler = new LinkedHashMap<>();

        for (Method method : type.getMethods()) {
            MessageSender sender = method.getAnnotation(MessageSender.class);

            //建立接口方法与具体实现方法的映射关系
            methodToHandler.put(method, new RocketMethodHandler(channelRepertory, sender));
        }

        return methodToHandler;
    }
}
