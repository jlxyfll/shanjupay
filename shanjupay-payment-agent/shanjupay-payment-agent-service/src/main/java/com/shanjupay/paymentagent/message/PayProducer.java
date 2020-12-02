package com.shanjupay.paymentagent.message;

import com.shanjupay.paymentagent.api.dto.PaymentResponseDTO;
import jdk.nashorn.internal.ir.annotations.Reference;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @title: PayProducer
 * @Author Tang Xiaojiang
 * @Date: 2020/11/30 15:50
 * @Version 1.0
 */
@Slf4j
@Component
public class PayProducer {
    // 订单结果查询topic
    private static final String TOPIC_ORDER = "TP_PAYMENT_ORDER";

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    public void payOrderNotice(PaymentResponseDTO result) {
        log.info("支付通知发送延迟消息：{}", result);
        try {
            // 处理消息存储格式
            Message<PaymentResponseDTO> message = MessageBuilder.withPayload(result).build();
            SendResult sendResult = rocketMQTemplate.syncSend(TOPIC_ORDER, message, 1000, 3);
        } catch (Exception e) {
            e.printStackTrace();
            log.warn(e.getMessage(), e);
        }
    }

    // 订单结果
    private static final String TOPIC_RESULT = "TP_PAYMENT_RESULT";

    public void payResultNotice(PaymentResponseDTO result) {
        rocketMQTemplate.convertAndSend(TOPIC_RESULT, result);
    }
}
