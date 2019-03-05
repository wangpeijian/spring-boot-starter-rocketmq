package citic.c.rocketmq.starter.config.bindings;

import citic.c.rocketmq.starter.enums.ProducerType;
import lombok.Data;

@Data
public class BaseConfig {

    private String topic;

    private ProducerType type = ProducerType.normal;

    public boolean isOrder() {
        return ProducerType.order == type;
    }

    public boolean isTransaction() {
        return ProducerType.transaction == type;
    }

    public boolean isNormal() {
        return ProducerType.normal == type;
    }
}
