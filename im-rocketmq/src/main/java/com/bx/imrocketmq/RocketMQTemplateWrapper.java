package com.bx.imrocketmq;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Wrapper of {@link RocketMQTemplate} to simplify send operations.
 */
@Component
public class RocketMQTemplateWrapper {

    @Autowired(required = false)
    private RocketMQTemplate rocketMQTemplate;

    /**
     * Send message using default converter
     */
    public void send(String topic, Object message) {
        if (rocketMQTemplate != null) {
            rocketMQTemplate.convertAndSend(topic, message);
        }
    }
}
