package citic.c.rocketmq.starter.processor;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * 实现spring生命周期钩子,根据配置文件注入生产者的bean
 */
@Configuration
public class ProducerProcessor implements BeanPostProcessor {


    private final ProducerRegister producerRegister;

    ProducerProcessor(ProducerRegister producerRegister) {
        this.producerRegister = producerRegister;
    }

    @PostConstruct
    private void afterConstruction() {
        //初始化生产者bean
        this.producerRegister.initChannelRepertory();
    }
}
