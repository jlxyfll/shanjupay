package com.shanjupay.test.rocketmq.message;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * @title: ConsumerSimple
 * @Author Tang Xiaojiang
 * @Date: 2020/11/29 22:24
 * @Version 1.0
 */
@Component
@RocketMQMessageListener(topic = "my-topic",consumerGroup = "demo-consumer-group")
public class ConsumerSimple implements RocketMQListener<String> {
    // 接收到消息接收此方法
    @Override
    public void onMessage(String message) {
        System.out.println(message);
    }
}
