package com.wpj.spring.boot.rocketmq.starter.config.bindings;

import com.wpj.spring.boot.rocketmq.starter.enums.ProducerType;
import lombok.Data;

@Data
class BaseConfig {

    private String groupId;

    private String topic;

    private ProducerType type = ProducerType.normal;
}
