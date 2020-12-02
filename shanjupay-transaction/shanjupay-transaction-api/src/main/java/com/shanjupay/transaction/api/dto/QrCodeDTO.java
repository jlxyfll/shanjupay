package com.shanjupay.transaction.api.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @title: QRCodeDto
 * @Author Tang Xiaojiang
 * @Date: 2020/11/27 16:22
 * @Version 1.0
 */
@Data
@NoArgsConstructor
@ApiModel(value="QrCodeDTO", description="二维码")
public class QrCodeDTO implements Serializable {
    private Long merchantId;
    private String appId;
    private Long storeId;
    // 商品标题
    private String subject;
    // 订单描述
    private String body;
}
