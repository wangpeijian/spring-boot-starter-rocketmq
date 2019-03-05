package citic.c.rocketmq.starter.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Component
public @interface MonitorRocketMsg {

    @AliasFor("consumerName")
    String value() default "";

    @AliasFor("value")
    String consumerName() default "";
}

