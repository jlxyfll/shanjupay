package com.shanjupay.test.rocketmq.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @title: OrderExt
 * @Author Tang Xiaojiang
 * @Date: 2020/11/30 11:04
 * @Version 1.0
 */
@Data
@NoArgsConstructor
@ToString
public class OrderExt implements Serializable {
    private String id;
    private Date createTime;
    private Long money;
    private String title;
}
