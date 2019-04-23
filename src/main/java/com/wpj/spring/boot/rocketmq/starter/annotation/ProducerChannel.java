package com.wpj.spring.boot.rocketmq.starter.annotation;

import java.lang.annotation.*;

/**
 * @auther wangpejian
 * @date 19-3-28 上午10:19
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface ProducerChannel {

}
