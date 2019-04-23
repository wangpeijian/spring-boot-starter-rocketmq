package com.wpj.spring.boot.rocketmq.starter.config.bindings;

import lombok.Data;

/**
 * 生产者配置
 */
@Data
public class ProducerConfig extends BaseConfig {

    private String tag;

}
