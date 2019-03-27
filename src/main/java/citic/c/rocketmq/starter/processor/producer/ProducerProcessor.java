package citic.c.rocketmq.starter.processor.producer;

import citic.c.rocketmq.starter.channel.ChannelBase;
import citic.c.rocketmq.starter.config.RocketMQConfig;
import citic.c.rocketmq.starter.config.bindings.Bindings;
import citic.c.rocketmq.starter.config.bindings.ProducerConfig;
import citic.c.rocketmq.starter.enums.ProducerType;
import citic.c.rocketmq.starter.repertory.ChannelRepertory;
import citic.c.rocketmq.starter.transaction.checker.AbstractChecker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * 实现spring生命周期钩子,根据配置文件注入生产者的bean
 */
@Slf4j
@Configuration
public class ProducerProcessor implements BeanPostProcessor {

    private final RocketMQConfig rocketMQConfig;

    private final DefaultListableBeanFactory beanFactory;

    private final ChannelRepertory channelRepertory;

    private final List<AbstractChecker> transactionCheckers;


    public ProducerProcessor(RocketMQConfig rocketMQConfig, ConfigurableApplicationContext applicationContext, List<AbstractChecker> transactionCheckers, ChannelRepertory channelRepertory) {
        this.rocketMQConfig = rocketMQConfig;
        this.beanFactory = (DefaultListableBeanFactory) applicationContext.getBeanFactory();
        this.transactionCheckers = transactionCheckers;
        this.channelRepertory = channelRepertory;
    }

    /**
     * 初始化生产者仓库
     */
    @PostConstruct
    void initChannel() {

        ProducerFactory producerFactory = new ProducerFactory(channelRepertory, transactionCheckers);

        if (rocketMQConfig == null) {
            return;
        }

        Bindings bindings = rocketMQConfig.getBindings();
        if (bindings == null) {
            return;
        }

        HashMap<String, ProducerConfig> producersConfig = bindings.getProducers();
        if (producersConfig == null) {
            return;
        }

        Set<Map.Entry<String, ProducerConfig>> configSet = producersConfig.entrySet();

        for (Map.Entry<String, ProducerConfig> entry : configSet) {
            String channelName = entry.getKey();
            ProducerConfig config = entry.getValue();
            String groupId = config.getGroupId();
            String topic = config.getTopic();
            String tag = config.getTag();
            ProducerType type = config.getType();

            //获取生产者配置对象
            Properties properties = rocketMQConfig.getProducerProperties(groupId);

            ChannelBase channel = producerFactory.createChannel(channelName, config, properties);

            beanFactory.registerSingleton(channelName, channel);
            log.debug("注入生产者 channelName: {}, type: {}, producerId: {}, topic: {}, tag: {}", channelName, type, groupId, topic, tag);
        }
    }
}
