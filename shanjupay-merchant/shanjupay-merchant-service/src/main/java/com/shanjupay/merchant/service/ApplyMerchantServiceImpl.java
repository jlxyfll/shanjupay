package com.shanjupay.merchant.service;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.merchant.api.ApplyMerchantService;
import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.convert.MerchantConvert;
import com.shanjupay.merchant.entity.Merchant;
import com.shanjupay.merchant.mapper.MerchantMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @title: ApplyMerchantImpl
 * @Author Tang Xiaojiang
 * @Date: 2020/11/22 10:37
 * @Version 1.0
 */
@Service
public class ApplyMerchantServiceImpl implements ApplyMerchantService {
    @Autowired
    private MerchantMapper merchantMapper;

    @Override
    public void applyMerchant(Long merchantId, MerchantDTO merchantDTO) throws BusinessException {
        // 接收资质审核信息，更新到商户表中
        // 参数校验
        if (merchantId == null || merchantDTO == null) {
            throw new BusinessException(CommonErrorCode.E_100108);
        }
        // 根据id查询商户
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(CommonErrorCode.E_200002);
        }
        Merchant merchantNew = MerchantConvert.INSTANCE.merchantDTO2Merchant(merchantDTO);
        // 将必要的参数设置到merchantNew
        merchantNew.setId(merchant.getId());
        merchantNew.setMobile(merchant.getMobile());
        // 已申请待审核状态
        merchantNew.setAuditStatus("1");
        merchantNew.setTenantId(merchant.getTenantId());
        // 更新
        merchantMapper.updateById(merchantNew);
    }
}
