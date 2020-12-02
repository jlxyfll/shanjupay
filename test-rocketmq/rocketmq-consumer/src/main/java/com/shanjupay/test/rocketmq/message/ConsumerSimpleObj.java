package com.shanjupay.test.rocketmq.message;

import com.shanjupay.test.rocketmq.model.OrderExt;
import org.apache.rocketmq.common.message.MessageExt;
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
@RocketMQMessageListener(topic = "my-topic-obj", consumerGroup = "demo-consumer-group-obj")
public class ConsumerSimpleObj implements RocketMQListener<MessageExt> {

//    @Override
//    public void onMessage(OrderExt orderExt) {
//        System.out.println(orderExt);
//    }

    @Override
    public void onMessage(MessageExt messageExt) {
        // 取出当前重试次数
        int reconsumeTimes = messageExt.getReconsumeTimes();
        System.out.println(reconsumeTimes);
        // 当大于一定的次数后将消息写入数据库，由单独的程序或人工去处理
        if (reconsumeTimes > 2) {
            // 将消息写入数据库，之后正常返回
            return;
        }
        throw new RuntimeException(String.format("第%s次处理失败...", reconsumeTimes));
    }
}
