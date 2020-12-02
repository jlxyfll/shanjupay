package com.shanjupay.test.rocketmq.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.shanjupay.test.rocketmq.model.OrderExt;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.Ordered;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ProducerSimpleTest {

    @Autowired
    private ProducerSimple producerSimple;

    @Test
    public void sendSyncMsg() {
        this.producerSimple.sendSyncMsg("my-topic", "第一条同步消息");
        System.out.println("end...");
    }

    @Test
    public void sendAsyncMsg() {
        this.producerSimple.sendAsyncMsg("my-topic", "第一条异步消息");
        System.out.println("end...");
        // 异步消息，为跟踪回调线程这里加入延迟
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void sendOneWayMsg() {
        this.producerSimple.sendOneWayMsg("my-topic", "第一条单向消息");
        System.out.println("end...");
    }

    @Test
    public void sendMsgByJson() {
        OrderExt orderExt = new OrderExt();
        orderExt.setId("0004");
        orderExt.setMoney(15667L);
        orderExt.setTitle("JSON数据重试");
        orderExt.setCreateTime(new Date());
        this.producerSimple.sendMsgByJson("my-topic-obj", orderExt);
        System.out.println(orderExt);
    }

    @Test
    public void sendMsgByJsonDelay() {
        OrderExt orderExt = new OrderExt();
        orderExt.setId("0002");
        orderExt.setMoney(1024L);
        orderExt.setTitle("JSON数据");
        orderExt.setCreateTime(new Date());
        this.producerSimple.sendMsgByJsonDelay("my-topic-obj", orderExt);
        System.out.println("end...");
    }

    @Test
    public void sendAsyncMsgByJsonDelay() throws InterruptedException, RemotingException, MQClientException, JsonProcessingException {
        OrderExt orderExt = new OrderExt();
        orderExt.setId("0003");
        orderExt.setMoney(1028L);
        orderExt.setTitle("JSON数据");
        orderExt.setCreateTime(new Date());
        this.producerSimple.sendAsyncMsgByJsonDelay("my-topic-obj", orderExt);
        System.out.println("end...");
        Thread.sleep(5000);
    }
}