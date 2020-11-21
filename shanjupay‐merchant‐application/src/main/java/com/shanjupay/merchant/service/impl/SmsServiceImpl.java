package com.shanjupay.merchant.service.impl;

import com.alibaba.fastjson.JSON;
import com.shanjupay.merchant.service.SmsService;
import com.shanjupay.merchant.vo.MerchantRegisterVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * @title: SmsServiceImpl
 * @Author Tang Xiaojiang
 * @Date: 2020/11/20 22:49
 * @Version 1.0
 */
@Service
@Slf4j
public class SmsServiceImpl implements SmsService {
    @Autowired
    private RestTemplate restTemplate;
    @Value("${sms.url}")
    private String smsUrl;

    @Value("${sms.effectiveTime}")
    private String effectiveTime;

    @Override
    public String sendMsg(String phone) {
        // 请求验证码地址
        String url = smsUrl + "/generate?name=sms&effectiveTime=" + effectiveTime;

        log.info("调用短信微服务发送验证码：url:{}", url);
        // 请求体
        Map<String, Object> body = new HashMap<>();
        body.put("mobile", phone);
        // 请求头
        HttpHeaders httpHeaders = new HttpHeaders();
        // 将格式设置为JSON
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        // 封装请求体
        HttpEntity entity = new HttpEntity(body, httpHeaders);
        // post请求
        Map responseMap = null;
        try {
            ResponseEntity<Map> exchange = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            log.info("调用短信微服务发送验证码: 返回值:{}", JSON.toJSONString(exchange));
            // 获取响应
            responseMap = exchange.getBody();
        } catch (RestClientException e) {
            e.printStackTrace();
            throw new RuntimeException("发送验证码出错");
        }
        // 取出body中的result数据
        if (responseMap == null || responseMap.get("result") == null) {
            throw new RuntimeException("发送验证码出错");
        }

        Map result = (Map) responseMap.get("result");
        String key = result.get("key").toString();
        System.out.println(key);
        return key;
    }

    @Override
    public void checkVerifiyCode(String verifiyKey, String verifiyCode) {
        // 校验验证码地址
        String url = smsUrl + "/verify?name=sms&verificationCode=" + verifiyCode + "&verificationKey=" + verifiyKey;
        log.info("调用短信微服务校验验证码：url:{}", url);
        Map responseMap = null;
        try {
            // 校验验证码
            ResponseEntity<Map> exchange = restTemplate.exchange(url, HttpMethod.POST, HttpEntity.EMPTY, Map.class);
            log.info("调用短信微服务校验验证码: 返回值:{}", JSON.toJSONString(exchange));
            // 获取响应 HttpEntity.EMPTY 无请求头与请求体
            responseMap = exchange.getBody();
        } catch (RestClientException e) {
            e.printStackTrace();
            throw new RuntimeException("发校验验证码出错");
        }

        // 取出body中的result数据
        if (responseMap == null || responseMap.get("result") == null || !(Boolean) responseMap.get("result")) {
            throw new RuntimeException("校验验证码出错");
        }

    }


}
