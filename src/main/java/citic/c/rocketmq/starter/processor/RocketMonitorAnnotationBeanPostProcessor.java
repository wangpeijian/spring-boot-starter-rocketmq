package citic.c.rocketmq.starter.processor;

import citic.c.rocketmq.starter.annotation.MonitorEnable;
import citic.c.rocketmq.starter.annotation.MonitorRocketMsg;
import citic.c.rocketmq.starter.config.RocketMQConfig;
import citic.c.rocketmq.starter.config.bindings.Bindings;
import citic.c.rocketmq.starter.config.bindings.ConsumerConfig;
import citic.c.rocketmq.starter.message.ResponseMessage;
import com.aliyun.openservices.ons.api.*;
import com.aliyun.openservices.ons.api.order.OrderAction;
import com.aliyun.openservices.ons.api.order.OrderConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

@Order()
@Slf4j
@Configuration
public class RocketMonitorAnnotationBeanPostProcessor implements ApplicationContextAware, InitializingBean {

    final private RocketMQConfig rocketMQConfig;
    private ConfigurableApplicationContext applicationContext;
    /**
     * 监听者目标类对象缓存
     */
    private HashMap<String, Object> targetClassCache = new HashMap<>();

    public RocketMonitorAnnotationBeanPostProcessor(RocketMQConfig rocketMQConfig) {
        this.rocketMQConfig = rocketMQConfig;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        Map<String, Object> beans = this.applicationContext.getBeansWithAnnotation(MonitorEnable.class);
        if (Objects.nonNull(beans)) {
            beans.forEach((key, bean) -> this.scanMonitorMethod(bean.getClass()));
        }
    }

    /**
     * 扫描目标类上是否存在回调监听方法
     *
     * @param targetClass
     */
    private void scanMonitorMethod(Class<?> targetClass) {
        Method[] uniqueDeclaredMethods = ReflectionUtils.getUniqueDeclaredMethods(targetClass);
        for (Method method : uniqueDeclaredMethods) {
            MonitorRocketMsg mergedAnnotation = AnnotatedElementUtils.findMergedAnnotation(method, MonitorRocketMsg.class);

            if (mergedAnnotation != null) {
                this.bindingMonitor(targetClass, method, mergedAnnotation);
            }
        }
    }

    /**
     * 对目标类实例结果做缓存,为反射调用回调方法时指定实例
     *
     * @param targetClass
     */
    private void cacheTargetClass(Class<?> targetClass) {
        if (!targetClassCache.containsKey(targetClass.getName())) {
            try {
                targetClassCache.put(targetClass.getName(), targetClass.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取缓存的目标类
     *
     * @param targetClass
     * @return
     */
    private Object getTargetClassInstance(Class<?> targetClass) {
        return applicationContext.getBean(targetClass);
    }

    /**
     * 创建消费者,绑定回调方法
     *
     * @param targetClass
     * @param method
     * @param mergedAnnotation
     */
    private void bindingMonitor(Class<?> targetClass, Method method, MonitorRocketMsg mergedAnnotation) {
        String consumerName = mergedAnnotation.consumerName();

        Bindings bindings = rocketMQConfig.getBindings();

        if (bindings == null) {
            return;
        }

        HashMap<String, ConsumerConfig> consumerConfig = bindings.getConsumers();

        if (consumerConfig != null && consumerConfig.containsKey(consumerName)) {
            //创建订阅类实例
            this.cacheTargetClass(targetClass);

            ConsumerConfig config = consumerConfig.get(consumerName);

            if (config.isNormal()) {

                this.listenNormalMsg(targetClass, method, config);

            } else if (config.isOrder()) {

                this.listenOrderMsg(targetClass, method, config);

            } else if (config.isTransaction()) {

                this.listenTransactionMsg(targetClass, method, config);
            } else {

                log.warn("consumerName: {} 使用了不支持的类型: {}", consumerName, config.getType());
            }

        } else {
            log.warn(String.format("%s 方法没有找到匹配的消费者配置信息: %s", method, consumerName));
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
        Properties properties = rocketMQConfig.getConsumerProperties(config.getConsumer());

        if (config.useBroadcast()) {
            properties.put(PropertyKeyConst.MessageModel, PropertyValueConst.BROADCASTING);
        } else {
            properties.put(PropertyKeyConst.MessageModel, PropertyValueConst.CLUSTERING);
        }

        Consumer consumer = ONSFactory.createConsumer(properties);

        try {
            //订阅消息
            consumer.subscribe(config.getTopic(), config.getSubExpression(), (message, context) -> {
                log.debug("方法: {}. consumer: {}. MsgID: {} TOP: {} TAG: {}", method.getName(), config.getConsumer(), message.getMsgID(), message.getTopic(), message.getTag());

                try {
                    Object targetClassInstance = this.getTargetClassInstance(targetClass);
                    //context 对象目前为空,且对象类型不同,所以直接传空,目前此字段无意义
                    method.invoke(targetClassInstance, new ResponseMessage(message, null));

                    //方法调用成功则回复正确结果
                    return Action.CommitMessage;

                } catch (Exception e) {
                    log.error("方法: {}. consumer: {}. 消费失败: {}", method, config.getConsumer(), e);
                    //捕获到异常,则当作消费失败需要重新发送
                    return Action.ReconsumeLater;
                }
            });

            consumer.start();

            log.debug("方法: {}. consumer: {} subExpression: {}, 订阅消息.", method, config.getConsumer(), config.getSubExpression());

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
        Properties properties = rocketMQConfig.getConsumerProperties(config.getConsumer());

        properties.put(PropertyKeyConst.SuspendTimeMillis, String.valueOf(config.getSuspendTimeMillis()));
        properties.put(PropertyKeyConst.MaxReconsumeTimes, String.valueOf(config.getMaxReconsumeTimes()));

        OrderConsumer orderConsumer = ONSFactory.createOrderedConsumer(properties);

        try {
            //订阅消息
            orderConsumer.subscribe(config.getTopic(), config.getSubExpression(), (message, context) -> {
                log.debug("方法: {}. consumer: {}. MsgID: {} TOP: {} TAG: {}", method.getName(), config.getConsumer(), message.getMsgID(), message.getTopic(), message.getTag());

                try {

                    Object targetClassInstance = this.getTargetClassInstance(targetClass);
                    //context 对象目前为空,且对象类型不同,所以直接传空,目前此字段无意义
                    method.invoke(targetClassInstance, new ResponseMessage(message, null));

                    //方法调用成功则回复正确结果
                    return OrderAction.Success;

                } catch (Exception e) {
                    log.error("方法: {}. consumer: {}. 消费失败: {}", method, config.getConsumer(), e);
                    //捕获到异常,则当作消费失败需要重新发送
                    return OrderAction.Suspend;
                }
            });

            orderConsumer.start();

            log.debug("方法: {}. consumer: {} subExpression: {}, 订阅消息.", method, config.getConsumer(), config.getSubExpression());

        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 监听事务消息
     *
     * @param targetClass
     * @param method
     * @param config
     */
    private void listenTransactionMsg(Class<?> targetClass, Method method, ConsumerConfig config) {
        this.listenNormalMsg(targetClass, method, config);
    }
}
