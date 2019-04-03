package citic.c.rocketmq.starter.processor.proxy;


import citic.c.rocketmq.starter.annotation.MessageSender;
import citic.c.rocketmq.starter.annotation.enums.MessageType;
import citic.c.rocketmq.starter.channel.ChannelNormal;
import citic.c.rocketmq.starter.channel.ChannelOrder;
import citic.c.rocketmq.starter.channel.ChannelTransaction;
import com.aliyun.openservices.ons.api.SendCallback;
import com.aliyun.openservices.ons.api.transaction.LocalTransactionExecuter;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @auther wangpejian
 * @date 19-3-29 下午5:16
 */
public class RocketMethodHandler implements MethodHandler {

    private Object channel;
    private MessageSender sender;

    RocketMethodHandler(Object channel, MessageSender sender) {
        this.channel = channel;
        this.sender = sender;
    }

    @Override
    public Object invoke(Object[] argv) throws Throwable {
        Class<?>[] classArr = getClassArray(argv);

        Method method2Invoke = getMethod(classArr);

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
    private Method getMethod(Class<?>[] classArr) throws Exception {
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
     * @param argv
     * @return
     */
    private Class<?>[] getClassArray(Object[] argv) {
        Class<?>[] classArr = Arrays.stream(argv).map(Object::getClass).toArray(Class[]::new);
        //发送参数第一个都是Object
        classArr[0] = Object.class;

        //事务消息第二个参数类型是 LocalTransactionExecuter
        if (channel instanceof ChannelTransaction) {
            classArr[1] = LocalTransactionExecuter.class;
        }

        //Async消息第二个参数类型是 SendCallback
        if (channel instanceof ChannelNormal) {
            classArr[1] = SendCallback.class;
        }

        return classArr;
    }
}
