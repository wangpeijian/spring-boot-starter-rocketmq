package citic.c.rocketmq.starter.annotation;

import citic.c.rocketmq.starter.processor.proxy.ProxyProcessor;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 启动类注释，扫描当前项目中所有ProducerChannel注解接口
 *
 * @auther wangpejian
 * @date 19-3-28 上午10:19
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(ProxyProcessor.class)
public @interface EnableRocketMQ {

    @AliasFor("producerName")
    String value() default "";

    @AliasFor("value")
    String producerName() default "";

}
