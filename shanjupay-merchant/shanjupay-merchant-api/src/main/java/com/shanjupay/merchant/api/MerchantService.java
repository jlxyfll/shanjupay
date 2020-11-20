package com.shanjupay.merchant.api;

import com.shanjupay.merchant.api.dto.MerchantDTO;

/**
 * @title: MerchantService
 * @Author Tang Xiaojiang
 * @Date: 2020/11/20 13:55
 * @Version 1.0
 */
public interface MerchantService {
    /**
     * 根据ID查询详细信息
     */
    MerchantDTO queryMerchantById(Long merchantId);
}
