package com.shanjupay.merchant.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.PhoneUtil;
import com.shanjupay.merchant.api.MerchantService;
import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.convert.MerchantConvert;
import com.shanjupay.merchant.entity.Merchant;
import com.shanjupay.merchant.mapper.MerchantMapper;
import org.apache.commons.lang.StringUtils;
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
    public MerchantDTO createMerchant(MerchantDTO merchantDTO) throws BusinessException {
        // 1.校验
        if (merchantDTO == null) {
            // 传入对象为空
            throw new BusinessException(CommonErrorCode.E_100108);
        }
        // 2.校验手机号
        if (StringUtils.isBlank(merchantDTO.getMobile())) {
            // 手机号为空
            throw new BusinessException(CommonErrorCode.E_100112);
        }
        // 3.校验手机号合法性
        if (!PhoneUtil.isMatches(merchantDTO.getMobile())) {
            // 手机号不合法
            throw new BusinessException(CommonErrorCode.E_100109);
        }
        // 4.联系人非空校验
        if (StringUtils.isBlank(merchantDTO.getUsername())) {
            // 联系人为空
            throw new BusinessException(CommonErrorCode.E_100110);
        }
        // 5.校验手机号唯一性
        LambdaQueryWrapper<Merchant> lambdaQueryWrapper = new LambdaQueryWrapper<Merchant>().eq(Merchant::getMobile, merchantDTO.getMobile());
        Integer count = merchantMapper.selectCount(lambdaQueryWrapper);
        if (count > 0) {
            // 手机号已存在
            throw new BusinessException(CommonErrorCode.E_100113);
        }

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
