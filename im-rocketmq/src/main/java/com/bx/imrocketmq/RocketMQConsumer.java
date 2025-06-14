package com.bx.imrocketmq;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * RocketMQ consumer base class
 */
public abstract class RocketMQConsumer<T> {

    /**
     * handle single message
     */
    public void onMessage(T data) {}

    /**
     * handle batch messages
     */
    public void onMessage(List<T> datas) {}

    /**
     * get generic class type
     */
    public Class<T> getMessageClass() {
        Type superClass = getClass().getGenericSuperclass();
        if (superClass instanceof ParameterizedType) {
            Type type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
            return (Class<T>) type;
        }
        return (Class<T>) Object.class;
    }
}
