package citic.c.rocketmq.starter.processor;

import citic.c.rocketmq.starter.channel.ChannelNormal;
import citic.c.rocketmq.starter.channel.ChannelOrder;
import citic.c.rocketmq.starter.channel.ChannelTransaction;
import citic.c.rocketmq.starter.config.RocketMQConfig;
import citic.c.rocketmq.starter.config.bindings.Bindings;
import citic.c.rocketmq.starter.config.bindings.ProducerConfig;
import citic.c.rocketmq.starter.enums.ProducerType;
import citic.c.rocketmq.starter.message.ResponseMessage;
import citic.c.rocketmq.starter.repertory.ChannelRepertory;
import citic.c.rocketmq.starter.transaction.checker.AbstractChecker;
import com.aliyun.openservices.ons.api.ONSFactory;
import com.aliyun.openservices.ons.api.Producer;
import com.aliyun.openservices.ons.api.order.OrderProducer;
import com.aliyun.openservices.ons.api.transaction.LocalTransactionChecker;
import com.aliyun.openservices.ons.api.transaction.TransactionProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * 生产者
 */
@Slf4j
@Component
public class ProducerRegister {

    private final RocketMQConfig rocketMQConfig;

    private final DefaultListableBeanFactory beanFactory;

    private final List<AbstractChecker> transactionCheckers;

    private final ChannelRepertory channelRepertory;

    //缓存生产者对象
    private final HashMap<String, Producer> producer = new HashMap<>();
    private final HashMap<String, OrderProducer> orderProducer = new HashMap<>();
    private final HashMap<String, TransactionProducer> transactionProducer = new HashMap<>();
    private HashMap<String, AbstractChecker> transactionCheckerMap;

    public ProducerRegister(RocketMQConfig rocketMQConfig, ConfigurableApplicationContext applicationContext, List<AbstractChecker> transactionCheckers, ChannelRepertory channelRepertory) {
        this.rocketMQConfig = rocketMQConfig;
        this.beanFactory = (DefaultListableBeanFactory) applicationContext.getBeanFactory();
        this.transactionCheckers = transactionCheckers;
        this.channelRepertory = channelRepertory;
    }

    /**
     * 整理localTransactionCheckers, 将checker放入map
     */
    @PostConstruct
    void initCheckerMap() {
        this.transactionCheckerMap = new HashMap<>(this.transactionCheckers.size());
        this.transactionCheckers.forEach(abstractChecker -> transactionCheckerMap.put(abstractChecker.getChannelName(), abstractChecker));
    }

    /**
     * 初始化生产者仓库
     */
    void initChannelRepertory() {

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
            String producerId = config.getProducer();
            String topic = config.getTopic();
            String tag = config.getTag();
            ProducerType type = config.getType();

            //获取生产者配置对象
            Properties properties = rocketMQConfig.getProducerProperties(config.getProducer());

            //创建消息生产者
            if (config.isOrder()) {

                OrderProducer orderProducer = this.getOrderProducer(producerId, properties);
                ChannelOrder channelOrder = channelRepertory.addChannelOrder(channelName, new ChannelOrder(orderProducer, topic, tag));

                beanFactory.registerSingleton(channelName, channelOrder);
                log.debug("注入生产者 channelName: {}, type: {}, producerId: {}, topic: {}, tag: {}", channelName, type, producerId, topic, tag);

            } else if (config.isTransaction()) {

                //查找注册的checker
                AbstractChecker checker = this.transactionCheckerMap.get(channelName);

                if (checker == null) {
                    log.warn("事务类型生产者 {} 没有配置LocalTransactionChecker", channelName);
                }

                TransactionProducer transactionProducer = this.getTransactionProducer(producerId, properties, msg -> {
                    log.debug("事务型生产者 channelName: {}, type: {}, producerId: {}, topic: {}, tag: {}. 收到checker请求: {}", channelName, type, producerId, topic, tag, msg);
                    assert checker != null;
                    return checker.check(new ResponseMessage(msg, null));
                });
                ChannelTransaction channelTransaction = channelRepertory.addChannelTransaction(channelName, new ChannelTransaction(transactionProducer, topic, tag));

                beanFactory.registerSingleton(channelName, channelTransaction);
                log.debug("注入生产者 channelName: {}, type: {}, producerId: {}, topic: {}, tag: {}", channelName, type, producerId, topic, tag);

            } else if (config.isNormal()) {

                Producer producer = this.getProducer(producerId, properties);
                ChannelNormal channelNormal = channelRepertory.addChannelNormal(channelName, new ChannelNormal(producer, topic, tag));

                beanFactory.registerSingleton(channelName, channelNormal);
                log.debug("注入生产者 channelName: {}, type: {}, producerId: {}, topic: {}, tag: {}", channelName, type, producerId, topic, tag);
            } else {
                log.warn("channelName: {}配置错误.不支持类型为: {}的消息.", channelName, type);
            }
        }
    }

    /**
     * 创建一个新的 OrderProducer 如果缓存中存在同一id的实例,则直接使用已有的实例引用
     *
     * @param producerId
     * @param properties
     * @return
     */
    private OrderProducer getOrderProducer(final String producerId, final Properties properties) {

        //判断是否已经有缓存的实例对象
        if (this.orderProducer.containsKey(producerId)) {
            return this.orderProducer.get(producerId);
        }

        //创建新实例
        OrderProducer orderProducer = ONSFactory.createOrderProducer(properties);
        orderProducer.start();

        //加入缓存
        this.orderProducer.put(producerId, orderProducer);

        return orderProducer;
    }

    /**
     * 创建一个新的 TransactionProducer 如果缓存中存在同一id的实例,则直接使用已有的实例引用
     *
     * @param producerId
     * @param properties
     * @return
     */
    private TransactionProducer getTransactionProducer(final String producerId, final Properties properties, final LocalTransactionChecker checker) {

        //判断是否已经有缓存的实例对象
        if (this.transactionProducer.containsKey(producerId)) {
            return this.transactionProducer.get(producerId);
        }

        //创建新实例
        TransactionProducer transactionProducer = ONSFactory.createTransactionProducer(properties, checker);
        transactionProducer.start();

        //加入缓存
        this.transactionProducer.put(producerId, transactionProducer);

        return transactionProducer;
    }

    /**
     * 创建一个新的 Producer 如果缓存中存在同一id的实例,则直接使用已有的实例引用
     *
     * @param producerId
     * @param properties
     * @return
     */
    private Producer getProducer(final String producerId, final Properties properties) {

        //判断是否已经有缓存的实例对象
        if (this.producer.containsKey(producerId)) {
            return this.producer.get(producerId);
        }

        //创建新实例
        Producer producer = ONSFactory.createProducer(properties);
        producer.start();

        //加入缓存
        this.producer.put(producerId, producer);

        return producer;
    }

}
