package citic.c.rocketmq.starter.config.bindings;

import lombok.Data;

/**
 * 生产者配置
 */
@Data
public class ProducerConfig extends BaseConfig {

    private String producer;

    private String tag;

}
