package com.wpj.spring.boot.rocketmq.starter.processor.proxy;


import com.aliyun.openservices.ons.api.SendCallback;
import com.aliyun.openservices.ons.api.transaction.LocalTransactionExecuter;
import com.wpj.spring.boot.rocketmq.starter.annotation.MessageSender;
import com.wpj.spring.boot.rocketmq.starter.annotation.enums.MessageType;
import com.wpj.spring.boot.rocketmq.starter.channel.ChannelNormal;
import com.wpj.spring.boot.rocketmq.starter.channel.ChannelOrder;
import com.wpj.spring.boot.rocketmq.starter.channel.ChannelTransaction;
import com.wpj.spring.boot.rocketmq.starter.repertory.ChannelRepertory;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author wangpejian
 * @date 19-3-29 下午5:16
 */
public class RocketMethodHandler implements MethodHandler {

    private ChannelRepertory channelRepertory;

    private MessageSender sender;

    RocketMethodHandler(ChannelRepertory channelRepertory, MessageSender sender) {
        this.channelRepertory = channelRepertory;
        this.sender = sender;
    }

    @Override
    public Object invoke(Object[] argv) throws Throwable {
        String channelName = sender.channel();
        Object channel = channelRepertory.findChannel(channelName);

        assert channel != null;

        Class<?>[] classArr = getClassArray(channel, argv);

        Method method2Invoke = getMethod(channel, classArr);

        assert method2Invoke != null;
        return method2Invoke.invoke(channel, argv);
    }

    /**
     * 获取调用方法
     *
     * @param classArr
     * @return
     * @throws Exception
     */
    private Method getMethod(Object channel, Class<?>[] classArr) throws Exception {
        String methodName = getMethodName();

        if (channel instanceof ChannelNormal) {
            return ChannelNormal.class.getMethod(methodName, classArr);
        } else if (channel instanceof ChannelOrder) {
            return ChannelOrder.class.getMethod(methodName, classArr);
        } else if (channel instanceof ChannelTransaction) {
            return ChannelTransaction.class.getMethod(methodName, classArr);
        }

        return null;
    }

    /**
     * 获取发送方法名
     *
     * @return
     */
    private String getMethodName() {
        //方法没添加注解，使用默认发送方式
        if (sender == null) {
            return MessageType.defaultType.getMethodName();
        }

        MessageType messageType = sender.type();
        return messageType.getMethodName();
    }

    /**
     * 调整并返回参数列表
     *
     *
     * @param channel
     * @param argv
     * @return
     */
    private Class<?>[] getClassArray(Object channel, Object[] argv) {
        Class<?>[] classArr = Arrays.stream(argv).map(Object::getClass).toArray(Class[]::new);
        //发送参数第一个都是Object
        classArr[0] = Object.class;

        //事务消息第二个参数类型是 LocalTransactionExecuter
        if (channel instanceof ChannelTransaction) {
            classArr[1] = LocalTransactionExecuter.class;
        }

        //Async消息第二个参数类型是 SendCallback
        if (channel instanceof ChannelNormal && MessageType.async.equals(sender.type())) {
            classArr[1] = SendCallback.class;
        }

        return classArr;
    }
}
