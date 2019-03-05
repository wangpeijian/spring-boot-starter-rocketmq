package citic.c.rocketmq.starter.channel;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.SendResult;
import com.aliyun.openservices.ons.api.order.OrderProducer;

/**
 * 顺序消息代理类
 */
public class ChannelOrder extends ChannelBase {

    private OrderProducer orderProducer;

    public ChannelOrder(OrderProducer orderProducer, String topic, String tag) {
        super(topic, tag);
        this.orderProducer = orderProducer;
    }

    public SendResult send(final Object message, final String shardingKey) {
        Message msg = this.getMessage(message);
        return orderProducer.send(msg, shardingKey);
    }
}
