package com.shanjupay.transaction.test;

import com.shanjupay.transaction.api.PayChannelService;
import com.shanjupay.transaction.api.dto.PayChannelDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * @title: PayChannelServiceTest
 * @Author Tang Xiaojiang
 * @Date: 2020/11/22 18:21
 * @Version 1.0
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class PayChannelServiceTest {
    @Autowired
    private PayChannelService payChannelService;

    // 测试根据服务类型查询支付渠道
    @Test
    public void queryPayChannelByPlatformChannel() {
        List<PayChannelDTO> shanju_c2b = payChannelService.queryPayChannelByPlatformChannel("shanju_c2b");
        System.out.println(shanju_c2b);
    }
}
