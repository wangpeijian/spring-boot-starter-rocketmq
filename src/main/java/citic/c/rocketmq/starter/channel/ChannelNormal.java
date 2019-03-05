package citic.c.rocketmq.starter.channel;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.Producer;
import com.aliyun.openservices.ons.api.SendCallback;
import com.aliyun.openservices.ons.api.SendResult;

import java.util.concurrent.ExecutorService;

/**
 * 普通消息,延时消息,定时消息的代理类
 */
public class ChannelNormal extends ChannelBase {

    private Producer producer;

    public ChannelNormal(Producer producer, String topic, String tag) {
        super(topic, tag);
        this.producer = producer;
    }

    /**
     * 支持延时,定时消息
     *
     * @param message
     * @param timeStamp
     */
    public void sendOneway(final Object message, final long timeStamp) {
        Message msg = this.getMessage(message);
        msg.setStartDeliverTime(timeStamp);
        producer.sendOneway(msg);
    }

    /**
     * 支持延时,定时消息
     *
     * @param message
     * @param timeStamp
     */
    public void sendAsync(final Object message, final SendCallback sendCallback, final long timeStamp) {
        Message msg = this.getMessage(message);
        msg.setStartDeliverTime(timeStamp);
        producer.sendAsync(msg, sendCallback);
    }

    public void setCallbackExecutor(final ExecutorService callbackExecutor) {
        producer.setCallbackExecutor(callbackExecutor);
    }

    /**
     * 支持延时,定时消息
     *
     * @param message
     * @param timeStamp
     */
    public SendResult send(final Object message, final long timeStamp) {
        Message msg = this.getMessage(message);
        msg.setStartDeliverTime(timeStamp);
        return producer.send(msg);
    }

    /**
     * 普通消息,没有延时
     *
     * @param message
     */
    public void sendOneway(final Object message) {
        this.sendOneway(message, 0);
    }

    /**
     * 普通消息,没有延时
     *
     * @param message
     */
    public void sendAsync(final Object message, final SendCallback sendCallback) {
        this.sendAsync(message, sendCallback, 0);
    }

    /**
     * 普通消息,没有延时
     *
     * @param message
     */
    public SendResult send(final Object message) {
        return this.send(message, 0);
    }
}
