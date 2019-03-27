package citic.c.rocketmq.starter.processor.consumer;

import citic.c.rocketmq.starter.config.RocketMQConfig;
import citic.c.rocketmq.starter.config.bindings.ConsumerConfig;
import citic.c.rocketmq.starter.message.ResponseMessage;
import com.aliyun.openservices.ons.api.*;
import com.aliyun.openservices.ons.api.order.OrderAction;
import com.aliyun.openservices.ons.api.order.OrderConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;

import java.lang.reflect.Method;
import java.util.Properties;

/**
 * @auther wangpejian
 * @date 19-3-27 下午2:50
 */
@Slf4j
class ConsumerFactory {

    private RocketMQConfig rocketMQConfig;

    private ConfigurableApplicationContext applicationContext;

    ConsumerFactory(ConfigurableApplicationContext applicationContext, RocketMQConfig rocketMQConfig) {
        this.applicationContext = applicationContext;
        this.rocketMQConfig = rocketMQConfig;
    }

    void initConsumer(Class<?> targetClass, Method method, ConsumerConfig config) {

        switch (config.getType()) {
            case normal:
            case transaction:
                listenNormalMsg(targetClass, method, config);
                break;
            case order:
                listenOrderMsg(targetClass, method, config);
                break;
            default:
                log.warn("consumer使用了不支持的类型: {}", config.getType());
                break;
        }
    }

    /**
     * 监听普通消息类型
     *
     * @param targetClass
     * @param method
     * @param config
     */
    private void listenNormalMsg(Class<?> targetClass, Method method, ConsumerConfig config) {
        String groupId = config.getGroupId();
        Properties properties = rocketMQConfig.getConsumerProperties(groupId);

        if (config.useBroadcast()) {
            properties.put(PropertyKeyConst.MessageModel, PropertyValueConst.BROADCASTING);
        } else {
            properties.put(PropertyKeyConst.MessageModel, PropertyValueConst.CLUSTERING);
        }

        Consumer consumer = ONSFactory.createConsumer(properties);

        try {
            //订阅消息
            consumer.subscribe(config.getTopic(), config.getSubExpression(), (message, context) -> {
                log.debug("方法: {}. consumer: {}. MsgID: {} TOP: {} TAG: {}", method.getName(), groupId, message.getMsgID(), message.getTopic(), message.getTag());

                try {
                    Object targetClassInstance = applicationContext.getBean(targetClass);
                    //context 对象目前为空,且对象类型不同,所以直接传空,目前此字段无意义
                    method.invoke(targetClassInstance, new ResponseMessage(message, null));

                    //方法调用成功则回复正确结果
                    return Action.CommitMessage;

                } catch (Exception e) {
                    log.error("方法: {}. consumer: {}. 消费失败: {}", method, groupId, e);
                    //捕获到异常,则当作消费失败需要重新发送
                    return Action.ReconsumeLater;
                }
            });

            consumer.start();
            log.debug("方法: {}. consumer: {} subExpression: {}, 订阅消息.", method, groupId, config.getSubExpression());

        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 监听顺序消息类型
     *
     * @param targetClass
     * @param method
     * @param config
     */
    private void listenOrderMsg(Class<?> targetClass, Method method, ConsumerConfig config) {
        String groupId = config.getGroupId();
        Properties properties = rocketMQConfig.getConsumerProperties(groupId);

        properties.put(PropertyKeyConst.SuspendTimeMillis, String.valueOf(config.getSuspendTimeMillis()));
        properties.put(PropertyKeyConst.MaxReconsumeTimes, String.valueOf(config.getMaxReconsumeTimes()));

        OrderConsumer orderConsumer = ONSFactory.createOrderedConsumer(properties);

        try {
            //订阅消息
            orderConsumer.subscribe(config.getTopic(), config.getSubExpression(), (message, context) -> {
                log.debug("方法: {}. consumer: {}. MsgID: {} TOP: {} TAG: {}", method.getName(), groupId, message.getMsgID(), message.getTopic(), message.getTag());

                try {

                    Object targetClassInstance = applicationContext.getBean(targetClass);
                    //context 对象目前为空,且对象类型不同,所以直接传空,目前此字段无意义
                    method.invoke(targetClassInstance, new ResponseMessage(message, null));

                    //方法调用成功则回复正确结果
                    return OrderAction.Success;

                } catch (Exception e) {
                    log.error("方法: {}. consumer: {}. 消费失败: {}", method, groupId, e);
                    //捕获到异常,则当作消费失败需要重新发送
                    return OrderAction.Suspend;
                }
            });

            orderConsumer.start();
            log.debug("方法: {}. consumer: {} subExpression: {}, 订阅消息.", method, groupId, config.getSubExpression());

        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }
}
