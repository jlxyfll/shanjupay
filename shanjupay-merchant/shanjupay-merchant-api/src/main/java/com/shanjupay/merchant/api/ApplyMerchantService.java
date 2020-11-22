package com.shanjupay.merchant.api;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.merchant.api.dto.MerchantDTO;

/**
 * @title: applyMerchant
 * @Author Tang Xiaojiang
 * @Date: 2020/11/22 10:28
 * @Version 1.0
 */
public interface ApplyMerchantService {
    /**
     * 资质申请
     * @param merchantId
     * @param merchantDTO
     * @throws BusinessException
     */
    public void applyMerchant(Long merchantId, MerchantDTO merchantDTO) throws BusinessException;
}
