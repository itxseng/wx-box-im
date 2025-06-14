package com.bx.imrocketmq;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RocketMQ consumer annotation
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RocketMQListener {
    /**
     * topic name
     */
    String topic();

    /**
     * consumer group name
     */
    String group();
}
