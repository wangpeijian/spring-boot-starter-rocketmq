package com.wpj.spring.boot.rocketmq.starter.config.bindings;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Data
@ConfigurationProperties(prefix = "rocket-mq.bindings")
@Component
public class Bindings {

    private HashMap<String, ProducerConfig> producers;

    private HashMap<String, ConsumerConfig> consumers;

}
