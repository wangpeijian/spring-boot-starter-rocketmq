package com.wpj.spring.boot.rocketmq.starter.channel;

import com.alibaba.fastjson.JSON;
import com.aliyun.openservices.ons.api.Message;
import lombok.Data;

/**
 * 发送者代理类的抽象类
 */
@Data
public abstract class ChannelBase {

    private String topic;

    private String tag;

    ChannelBase(String topic, String tag) {
        this.topic = topic;
        this.tag = tag;
    }

    /**
     * 将消息体以json方式序列化
     *
     * @param messageBody
     * @return
     */
    private byte[] serializeMessageBody(final Object messageBody) {
        return JSON.toJSONString(messageBody).getBytes();
    }

    /**
     * 获取发送的消息对象
     *
     * @param messageBody
     * @return
     */
    Message getMessage(final Object messageBody) {
        return new Message( //
                // Message 所属的 Topic
                this.getTopic(),
                // Message Tag 可理解为 Gmail 中的标签，对消息进行再归类，方便 Consumer 指定过滤条件在 MQ 服务器过滤
                this.getTag(),
                // Message Body 可以是任何二进制形式的数据， MQ 不做任何干预，
                // 需要 Producer 与 Consumer 协商好一致的序列化和反序列化方式
                this.serializeMessageBody(messageBody)
        );
    }
}
