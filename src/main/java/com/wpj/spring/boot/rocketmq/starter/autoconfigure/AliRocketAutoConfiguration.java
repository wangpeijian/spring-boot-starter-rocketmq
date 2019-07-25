package com.wpj.spring.boot.rocketmq.starter.autoconfigure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author wangpejian
 * @date 19-7-22 下午4:39
 */
@Slf4j
@Configuration
@ComponentScan({"com.wpj"})
@Import({com.wpj.spring.boot.rocketmq.starter.processor.proxy.ProxyProcessor.class})
public class AliRocketAutoConfiguration {

}
