package com.shanjupay.merchant.service;

import com.shanjupay.merchant.api.MerchantService;
import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.convert.MerchantConvert;
import com.shanjupay.merchant.entity.Merchant;
import com.shanjupay.merchant.mapper.MerchantMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @title: MerchantServiceImpl
 * @Author Tang Xiaojiang
 * @Date: 2020/11/20 15:05
 * @Version 1.0
 */
@Service
public class MerchantServiceImpl implements MerchantService {

    @Autowired
    private MerchantMapper merchantMapper;

    /**
     * 根据ID查询详细信息
     *
     * @param merchantId
     * @return
     */
    @Override
    public MerchantDTO queryMerchantById(Long merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        MerchantDTO merchantDTO = new MerchantDTO();
        merchantDTO.setId(merchant.getId());
        merchantDTO.setMerchantName(merchant.getMerchantName());
        return merchantDTO;
    }

    /**
     * 注册商户接口实现，用来接收服务注册时的手机号，账号，密码
     *
     * @param merchantDTO
     * @return
     */
    @Override
    public MerchantDTO createMerchant(MerchantDTO merchantDTO) {
        /*Merchant merchant = new Merchant();
        // 获取注册时的手机号
        String merchantDTOMobile = merchantDTO.getMobile();
        merchant.setMobile(merchantDTOMobile);
        // 设置商户状态为0-未审核
        merchant.setAuditStatus("0");
        merchantMapper.insert(merchant);
        // 将新增商户的ID返回
        Long merchantId = merchant.getId();
        merchantDTO.setId(merchantId);
        return merchantDTO;*/
        // 将dto转化为entity
        Merchant merchant = MerchantConvert.INSTANCE.merchantDTO2Merchant(merchantDTO);
        // 设置审核信息
        merchant.setAuditStatus("0");
        // 保存商户
        merchantMapper.insert(merchant);

        // 将entity转成DTO
        MerchantDTO merchantDTONew = MerchantConvert.INSTANCE.merchant2MerchantDTO(merchant);
        return merchantDTONew;
    }
}
