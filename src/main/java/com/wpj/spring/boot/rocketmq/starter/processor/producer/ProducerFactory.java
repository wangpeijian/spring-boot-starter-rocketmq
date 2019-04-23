package com.wpj.spring.boot.rocketmq.starter.processor.producer;

import com.aliyun.openservices.ons.api.ONSFactory;
import com.aliyun.openservices.ons.api.Producer;
import com.aliyun.openservices.ons.api.order.OrderProducer;
import com.aliyun.openservices.ons.api.transaction.LocalTransactionChecker;
import com.aliyun.openservices.ons.api.transaction.TransactionProducer;
import com.wpj.spring.boot.rocketmq.starter.channel.ChannelBase;
import com.wpj.spring.boot.rocketmq.starter.channel.ChannelNormal;
import com.wpj.spring.boot.rocketmq.starter.channel.ChannelOrder;
import com.wpj.spring.boot.rocketmq.starter.channel.ChannelTransaction;
import com.wpj.spring.boot.rocketmq.starter.config.bindings.ProducerConfig;
import com.wpj.spring.boot.rocketmq.starter.enums.ProducerType;
import com.wpj.spring.boot.rocketmq.starter.message.ResponseMessage;
import com.wpj.spring.boot.rocketmq.starter.repertory.ChannelRepertory;
import com.wpj.spring.boot.rocketmq.starter.transaction.checker.AbstractChecker;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Properties;

/**
 * @auther wangpejian
 * @date 19-3-27 下午3:38
 */
@Slf4j
public class ProducerFactory {

    private final static HashMap<String, AbstractChecker> transactionCheckerMap = new HashMap<>();
    private final ChannelRepertory channelRepertory;

    //缓存生产者对象
    private final HashMap<String, Producer> producerCache = new HashMap<>();
    private final HashMap<String, OrderProducer> orderProducerCache = new HashMap<>();
    private final HashMap<String, TransactionProducer> transactionProducerCache = new HashMap<>();


    ProducerFactory(ChannelRepertory channelRepertory, List<AbstractChecker> transactionCheckers) {
        this.channelRepertory = channelRepertory;
        transactionCheckers.forEach(abstractChecker -> transactionCheckerMap.put(abstractChecker.getChannelName(), abstractChecker));
    }

    ChannelBase createChannel(String channelName, ProducerConfig config, Properties properties) {

        String groupId = config.getGroupId();
        String topic = config.getTopic();
        String tag = config.getTag();
        ProducerType type = config.getType();

        switch (config.getType()) {
            case normal:

                Producer producer = getProducer(groupId, properties);
                return channelRepertory.addChannelNormal(channelName, new ChannelNormal(producer, topic, tag));

            case order:

                OrderProducer orderProducer = getOrderProducer(groupId, properties);
                return channelRepertory.addChannelOrder(channelName, new ChannelOrder(orderProducer, topic, tag));

            case transaction:

                //查找注册的checker
                AbstractChecker checker = transactionCheckerMap.get(channelName);

                if (checker == null) {
                    log.warn("事务类型生产者 {} 没有配置LocalTransactionChecker", channelName);
                }

                TransactionProducer transactionProducer = getTransactionProducer(groupId, properties, msg -> {
                    log.debug("事务型生产者 channelName: {}, type: {}, producerId: {}, topic: {}, tag: {}. 收到checker请求: {}", channelName, type, groupId, topic, tag, msg);
                    assert checker != null;
                    return checker.check(new ResponseMessage(msg, null));
                });

                return channelRepertory.addChannelTransaction(channelName, new ChannelTransaction(transactionProducer, topic, tag));

            default:
                log.warn("channelName: {}配置错误.不支持类型为: {}的消息.", channelName, type);
                return null;
        }
    }

    /**
     * 创建一个新的 OrderProducer 如果缓存中存在同一id的实例,则直接使用已有的实例引用
     *
     * @param groupId
     * @param properties
     * @return
     */
    private OrderProducer getOrderProducer(final String groupId, final Properties properties) {

        //判断是否已经有缓存的实例对象
        if (orderProducerCache.containsKey(groupId)) {
            return orderProducerCache.get(groupId);
        }

        //创建新实例
        OrderProducer orderProducer = ONSFactory.createOrderProducer(properties);
        orderProducer.start();

        //加入缓存
        orderProducerCache.put(groupId, orderProducer);

        return orderProducer;
    }

    /**
     * 创建一个新的 TransactionProducer 如果缓存中存在同一id的实例,则直接使用已有的实例引用
     *
     * @param groupId
     * @param properties
     * @return
     */
    private TransactionProducer getTransactionProducer(final String groupId, final Properties properties, final LocalTransactionChecker checker) {

        //判断是否已经有缓存的实例对象
        if (transactionProducerCache.containsKey(groupId)) {
            return transactionProducerCache.get(groupId);
        }

        //创建新实例
        TransactionProducer transactionProducer = ONSFactory.createTransactionProducer(properties, checker);
        transactionProducer.start();

        //加入缓存
        transactionProducerCache.put(groupId, transactionProducer);

        return transactionProducer;
    }

    /**
     * 创建一个新的 Producer 如果缓存中存在同一id的实例,则直接使用已有的实例引用
     *
     * @param groupId
     * @param properties
     * @return
     */
    private Producer getProducer(final String groupId, final Properties properties) {

        //判断是否已经有缓存的实例对象
        if (producerCache.containsKey(groupId)) {
            return producerCache.get(groupId);
        }

        //创建新实例
        Producer producer = ONSFactory.createProducer(properties);
        producer.start();

        //加入缓存
        producerCache.put(groupId, producer);

        return producer;
    }
}
