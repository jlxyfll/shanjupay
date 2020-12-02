package com.shanjupay.paymentagent.message;

import com.alibaba.fastjson.JSON;
import com.shanjupay.paymentagent.api.PayChannelAgentService;
import com.shanjupay.paymentagent.api.conf.AliConfigParam;
import com.shanjupay.paymentagent.api.conf.WXConfigParam;
import com.shanjupay.paymentagent.api.dto.PaymentResponseDTO;
import com.shanjupay.paymentagent.api.dto.TradeStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

/**
 * @title: PayConsume
 * @Author Tang Xiaojiang
 * @Date: 2020/11/30 15:59
 * @Version 1.0
 */
@Slf4j
@Service
@RocketMQMessageListener(topic = "TP_PAYMENT_ORDER", consumerGroup = "CID_PAYMENT_CONSUMER")
public class PayConsumer implements RocketMQListener<MessageExt> {
    @Resource
    private PayChannelAgentService payChannelAgentService;

    @Autowired
    private PayProducer payProducer;

    @Override
    public void onMessage(MessageExt messageExt) {

        log.info("开始消费支付结果查询消息:{}", messageExt);
        // 取出消息内容
        String body = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        PaymentResponseDTO paymentResponseDTO = JSON.parseObject(body, PaymentResponseDTO.class);
        String outTradeNo = paymentResponseDTO.getOutTradeNo();
        String msg = paymentResponseDTO.getMsg();
        String param = String.valueOf(paymentResponseDTO.getContent());
        AliConfigParam aliConfigParam = JSON.parseObject(param, AliConfigParam.class);
        // 判断是支付宝还是微信
        PaymentResponseDTO result = new PaymentResponseDTO();
        if ("ALIPAY_WAP".equals(msg)) {
            // 查询支付宝支付结果
            result = payChannelAgentService.queryPayOrderByAli(aliConfigParam, outTradeNo);
        } else if ("WX_JSAPI".equals(msg)) {
            // 查询微信支付结果
            WXConfigParam wxConfigParam = JSON.parseObject(param, WXConfigParam.class);
            result = payChannelAgentService.queryPayOrderByWeChat(wxConfigParam, outTradeNo);
        }
        // 返回查询获得的支付状态
        if (TradeStatus.UNKNOWN.equals(result.getTradeState()) || TradeStatus.USERPAYING.equals(result.getTradeState())) {
            log.info("支付代理‐‐‐支付状态未知，等待重试");
            throw new RuntimeException("支付状态未知，等待重试");
        }

        log.info("交易中心处理支付结果通知，支付代理发送消息:{}", result);
        payProducer.payResultNotice(result);
    }
}
