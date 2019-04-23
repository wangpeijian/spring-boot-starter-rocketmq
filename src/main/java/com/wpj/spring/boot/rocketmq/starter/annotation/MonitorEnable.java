package com.wpj.spring.boot.rocketmq.starter.annotation;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 标记类中有消费者的方法
 * 注册消费者时扫描带有此注解的类,找到类下所有带有
 *
 * @MonitorRocketMsg 注解的方法, 注册为消费者消费方法
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Order
@Component
public @interface MonitorEnable {
}
