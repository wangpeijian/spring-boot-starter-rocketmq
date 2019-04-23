package com.wpj.spring.boot.rocketmq.starter.annotation;

import com.wpj.spring.boot.rocketmq.starter.annotation.enums.MessageType;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @auther wangpejian
 * @date 19-3-28 上午10:19
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MessageSender {

    @AliasFor("type")
    MessageType value() default MessageType.defaultType;

    @AliasFor("value")
    MessageType type() default MessageType.defaultType;

}
