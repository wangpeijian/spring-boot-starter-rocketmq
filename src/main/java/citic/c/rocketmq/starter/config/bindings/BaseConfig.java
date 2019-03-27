package citic.c.rocketmq.starter.config.bindings;

import citic.c.rocketmq.starter.enums.ProducerType;
import lombok.Data;

@Data
class BaseConfig {

    private String groupId;

    private String topic;

    private ProducerType type = ProducerType.normal;
}
