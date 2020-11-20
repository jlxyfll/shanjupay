package com.shanjupay.merchant;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * @title: RestTemplateTest
 * @Author Tang Xiaojiang
 * @Date: 2020/11/20 20:01
 * @Version 1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class RestTemplateTest {
    @Autowired
    private RestTemplate restTemplate;

    @Test
    public void restTest() {
        String url = "https://my.oschina.net/u/4283481/blog/4133344";
        String url1 = "https://www.baidu.com/";
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(url1, String.class);
        String body = responseEntity.getBody();
        System.out.println(body);
    }

    @Test
    public void testGetSmsCode() {
        // 请求验证码地址
        String url = "http://localhost:56085/sailing/generate?name=sms&effectiveTime=300";
        // 手机号
        String phone = "13245678451";
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
            log.info(e.getMessage(), e);
        }
        // 取出body中的result数据
        if (responseMap != null && responseMap.get("result") != null) {
            Map result = (Map) responseMap.get("result");
            String key = result.get("key").toString();
            System.out.println(key);
        }
    }
}
