package citic.c.rocketmq.starter.config.bindings;

import lombok.Data;

/**
 * 消费者配置
 */
@Data
public class ConsumerConfig extends BaseConfig {

    private String consumer;

    //普通消息类型配置项
    private boolean useBroadcast = false;

    // 顺序消息消费失败进行重试前的等待时间，单位(毫秒)
    private long suspendTimeMillis = 100;

    // 消息消费失败时的最大重试次数
    private long MaxReconsumeTimes = 20;

    private String subExpression = "*";

    public boolean useBroadcast() {
        return this.useBroadcast;
    }

}
