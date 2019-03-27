package citic.c.rocketmq.starter.processor.consumer;

import citic.c.rocketmq.starter.annotation.MonitorEnable;
import citic.c.rocketmq.starter.annotation.MonitorRocketMsg;
import citic.c.rocketmq.starter.config.RocketMQConfig;
import citic.c.rocketmq.starter.config.bindings.Bindings;
import citic.c.rocketmq.starter.config.bindings.ConsumerConfig;
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

@Order()
@Slf4j
@Configuration
public class ConsumerProcessor implements ApplicationContextAware, InitializingBean {

    final private RocketMQConfig rocketMQConfig;
    private ConfigurableApplicationContext applicationContext;

    /**
     * 消费者组装工厂
     */
    private ConsumerFactory consumerFactory;

    public ConsumerProcessor(RocketMQConfig rocketMQConfig) {
        this.rocketMQConfig = rocketMQConfig;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }

    /**
     * 初始化缓存对象，工厂对象
     */
    private void processorInit() {
        this.consumerFactory = new ConsumerFactory(applicationContext, rocketMQConfig);
    }

    @Override
    public void afterPropertiesSet() {
        //初始化
        processorInit();

        //扫描全部监听者对象，注册消费者
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(MonitorEnable.class);
        if (Objects.nonNull(beans)) {
            beans.forEach((key, bean) -> scanMonitorMethod(bean.getClass()));
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
     * 创建消费者,绑定回调方法
     *
     * @param targetClass
     * @param method
     * @param mergedAnnotation
     */
    private void bindingMonitor(Class<?> targetClass, Method method, MonitorRocketMsg mergedAnnotation) {
        String consumerName = mergedAnnotation.consumerName();

        Bindings bindings = rocketMQConfig.getBindings();

        if (bindings == null || bindings.getConsumers() == null) {
            return;
        }

        HashMap<String, ConsumerConfig> consumerConfig = bindings.getConsumers();

        if (consumerConfig.containsKey(consumerName)) {

            ConsumerConfig config = consumerConfig.get(consumerName);
            consumerFactory.initConsumer(targetClass, method, config);

        } else {
            log.warn(String.format("%s 方法没有找到匹配的消费者配置信息: %s", method, consumerName));
        }

    }


}
