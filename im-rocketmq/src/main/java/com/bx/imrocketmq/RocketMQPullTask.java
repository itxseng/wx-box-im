package com.bx.imrocketmq;

import com.alibaba.fastjson.JSON;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

/**
 * Start RocketMQ consumers based on {@link RocketMQListener} annotations.
 */
@Slf4j
@Component
public class RocketMQPullTask implements CommandLineRunner {

    @Autowired(required = false)
    private List<RocketMQConsumer> consumers = Collections.emptyList();

    @Value("${rocketmq.name-server:}")
    private String nameServer;

    @Override
    public void run(String... args) {
        consumers.forEach(consumer -> {
            RocketMQListener anno = consumer.getClass().getAnnotation(RocketMQListener.class);
            if (anno == null) {
                return;
            }
            DefaultMQPushConsumer pushConsumer = new DefaultMQPushConsumer(anno.group());
            pushConsumer.setNamesrvAddr(nameServer);
            pushConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
            try {
                pushConsumer.subscribe(anno.topic(), "*");
                pushConsumer.registerMessageListener(new MessageListenerConcurrently() {
                    @Override
                    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
                                                                    ConsumeConcurrentlyContext context) {
                        for (MessageExt ext : msgs) {
                            String json = new String(ext.getBody(), StandardCharsets.UTF_8);
                            Object obj = JSON.parseObject(json, consumer.getMessageClass());
                            consumer.onMessage(obj);
                        }
                        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                    }
                });
                pushConsumer.start();
            } catch (MQClientException e) {
                log.error("start consumer error", e);
            }
        });
    }

    @PreDestroy
    public void destroy() {
        // nothing to destroy here, placeholder for symmetry
    }
}
