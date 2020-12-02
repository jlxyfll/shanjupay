package com.shanjupay.test.rocketmq.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.shanjupay.test.rocketmq.model.OrderExt;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;

/**
 * @title: ProducerSimple
 * @Author Tang Xiaojiang
 * @Date: 2020/11/28 22:52
 * @Version 1.0
 */
@Component
public class ProducerSimple {
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 发送同步消息
     *
     * @param topic
     * @param msg
     */
    public void sendSyncMsg(String topic, String msg) {
        rocketMQTemplate.syncSend(topic, msg);
    }

    /**
     * 发送异步消息
     *
     * @param topic
     * @param msg
     */
    public void sendAsyncMsg(String topic, String msg) {
        rocketMQTemplate.asyncSend(topic, msg, new SendCallback() {
            // 成功回调
            @Override
            public void onSuccess(SendResult sendResult) {
                System.out.println(sendResult.getSendStatus());
            }

            // 异常回调
            @Override
            public void onException(Throwable e) {
                System.out.println(e.getMessage());
            }
        });
    }

    /**
     * 发送单向消息
     *
     * @param topic
     * @param msg
     */
    public void sendOneWayMsg(String topic, String msg) {
        rocketMQTemplate.sendOneWay(topic, msg);
    }

    /**
     * 发送json消息
     *
     * @param topic
     * @param orderExt
     */
    public void sendMsgByJson(String topic, OrderExt orderExt) {
        rocketMQTemplate.convertAndSend(topic, orderExt);
    }

    /**
     * 发送同步延迟消息
     *
     * @param topic
     * @param orderExt
     */
    public void sendMsgByJsonDelay(String topic, OrderExt orderExt) {
        // 发送同步消息，消息内容将orderExt转成json
        Message<OrderExt> message = MessageBuilder.withPayload(orderExt).build();
        // 指定发送超时时间（毫秒）和延迟等级
        this.rocketMQTemplate.syncSend(topic, message, 1000, 3);
        System.out.printf("send msg:%S", orderExt);
    }

    /**
     * 发送异步延迟消息
     *
     * @param topic
     * @param orderExt
     */
    public void sendAsyncMsgByJsonDelay(String topic, OrderExt orderExt) throws JsonProcessingException, RemotingException, MQClientException, InterruptedException {
        // 发送异步消息，消息内容将orderExt转成json
        String json = rocketMQTemplate.getObjectMapper().writeValueAsString(orderExt);
        org.apache.rocketmq.common.message.Message message =
                new org.apache.rocketmq.common.message.Message(topic, json.getBytes(Charset.forName("utf-8")));
        // 设置延迟等级
        message.setDelayTimeLevel(3);
        // 发送异步消息
        rocketMQTemplate.getProducer().send(message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                // 成功回调
                System.out.println(sendResult);
            }

            @Override
            public void onException(Throwable e) {
                // 失败回调
                System.out.println(e.getMessage());
            }
        });
        // 指定发送超时时间（毫秒）和延迟等级
        System.out.printf("send msg:%S", orderExt);
    }


}
