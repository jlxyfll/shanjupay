package com.shanjupay.paymentagent.service;

import com.shanjupay.paymentagent.api.PayChannelAgentService;
import com.shanjupay.paymentagent.api.conf.AliConfigParam;
import com.shanjupay.paymentagent.api.dto.PaymentResponseDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class PayChannelAgentServiceImplTest {
    @Autowired
    private PayChannelAgentService payChannelAgentService;

    @Test
    public void createPayOrderByAliWAP() {
    }

    @Test
    public void queryPayOrderByAli() {
        /**
         * 应用id
         */
        String APP_ID = "2016110300789108";
        /**
         * 私钥
         */
        String APP_PRIVATE_KEY = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCcnHYKKv+VzB2yqqTbtOd201VylU9PAwB57GsgTEQQgSDkbUTVOij8g2HQgRqlFmTqDSPFGr0vJKkm9oHC5bdPzt8rT2OlNiFQv/fUjweKjDBU5575wGgIW99PzSlJp+M0ZEyol1khCkaqemai8cZS6IOScHmZ0+maId2XTwkTMewEMPfhOxjcTbLLtkbwl/vmoXK8/F9PuazM779x+t5KXTO0qq5E0L9PzFRbgWCFYn6XYEEsFrCZWQ8UkIKKU7wMFYqWEGQYmbMXmqCZP67twHi+7KSyU1epryfVlyMQF3qRkQgUYNHU8ews9mOhKQr7OKTx76GR1+mlC+7WdfqxAgMBAAECggEAM5VNQ9gZyusqNPkDx+H0Ay1kjZMkRjAE7iOyvTahjJYbkWF0NI8thM55X0XG0u1nW7fsOjWubmKy3ESBRQKIuF65HvjNJ1OG8aPpKzfZEgdOvAh2UmjPW0F3cj9vA/WqXk1S+oyvLLTHxcr/F1hvLEutWoR1aPdlkADrjbTYlltqhdy2yXZPS3FspUhoKl6vFm9FzWNBb/v0GtuXKuKw+oZN+M29ambKBYsEreSCKuCN7MGrfot40orG6XErvHKpBvLnIAp5M4fL28/C8z89dyyaZkVk8qf811FSdeC27AU0jzW/sRzzw1lV/LZ+XAnp5bpxbKiwwSPNuzRSzjblKQKBgQD0mWiyVS/jAhugw95B8bdXSW6Y9e6G80bb6UNknQJDrNTTGX1MHBP19h0sz68ymwMz8U5+VuWSyV6o5SV8uIGnLK6+JrbJ39GY9cW/joVYLWn786Q7upPaxKrr+u5vnZT3Qx6nGneYoAewhI8P/5iUqvmJz/zPRIDv/4S+10/5gwKBgQCj6SqmlGdThx9zLPOik3GYw9UuZj51oKv6VzZmSFel3hSxi9WeyI3de9/xJcR4EZJBHy8NGDv2rP9KsTU2wa/ccxngcVLN1Z/DX1byCVmUju4g63LIFKaEhEO1bIRy4BEU133xakjNC0x2LBYTfQifrwww8Ot5vLG2y1XsP6TouwKBgCz/4DQW2gWAiMat4kDtkFZdqxw2+pgXP60LoBtti1IaBNmFcfjDnaR9j+aDmu7Ld+5hcZhqsjesFl8qMRhd9XRlEwzmoHBJXg0vAh94v+12BW6V51wZukPgNLXPZ3hfXF2VGpmRk/kfT+rYqF1AH7kSL3B8ND2Jk9MThj956zuzAoGAJpBGK5kA0mMZICT0UUy1TkbM0jTV/yDFNYntT5U3odulLLyEju4KipCJeJf1KHah3VZz4HXSLdA35B/bMD3TnV8rOGYRIEJhazr0Hs/aVpxmJDKg97f8EXN45qdYZhqXijrE89aYuD0vSSJIM1+YdtFdYepczHB7s1EVrxauOt8CgYEAjmf2A4Cu7Fb4subA19AB1QJl3Cr8UUiQ+j3OqCfKDAevTQvF2HteZB/ipV93quwFog9XppYWWZDbjagRwyFVBYQQeP5EttaVkOW+SzEhhXkwLzNR/grIqtzTVc7LI9JsBL+PzpTZJJYrCjAENu4AqTS7i7rQzg4pkicvIDbeB0k=";
        /**
         * 公钥
         */
        String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlzeJfPCXHpETVC40zqD5IpbV3Es+Z7NIlyAH8FlFARSzKhaaiURo9OCMsuTMQ596hxIv/5IwKKowKk2K/lUTN7bvzYEdN3N6mANqwuRuof8V67xjUk9jwL6OYKHREJf5dAxktzfZhkbFZ3UwGDEwfw9qVRXmdjOMJifiy01xZj4rSR2Y/Q16WKqvm+JUsvx+OnSj8uHXoUtM1O9NfbQAO/OrMcD3/DbixNRa4nStSXkhwMfAKCiKNhEZ9L6IViqJ27sJ/FGGmhzmvv7Xa6m3WSeD6agmyFgkdOoXWHX/IY4mAvh2RZNK73TFAAWenqIcmLBxGxgNc4a583H564eMDQIDAQAB";
        /**
         * 编码
         */
        String CHARSET = "utf-8";
        /**
         * 支付宝网关地址
         */
        String serverUrl = "https://openapi.alipaydev.com/gateway.do";
        /**
         * 签名算法的名称
         */
        String signType = "RSA2";
        String format = "json";


        // 支付渠道参数
        AliConfigParam aliConfigParam = new AliConfigParam();
        aliConfigParam.setUrl(serverUrl);
        aliConfigParam.setCharest(CHARSET);
        aliConfigParam.setAlipayPublicKey(ALIPAY_PUBLIC_KEY);
        aliConfigParam.setRsaPrivateKey(APP_PRIVATE_KEY);
        aliConfigParam.setAppId(APP_ID);
        aliConfigParam.setFormat(format);
        aliConfigParam.setSigntype(signType);
        PaymentResponseDTO paymentResponseDTO = payChannelAgentService.queryPayOrderByAli(aliConfigParam, "SJ1333310526050304000");
        System.out.println(paymentResponseDTO);
    }
}