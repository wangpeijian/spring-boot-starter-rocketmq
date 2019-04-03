package citic.c.rocketmq.starter.annotation;

import org.springframework.core.annotation.AliasFor;

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

    @AliasFor("producerName")
    String value() default "";

    @AliasFor("value")
    String producerName() default "";

}
