package com.shanjupay.merchant.convert;

import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.vo.MerchantRegisterVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;


/**
 * @title: MerchantCovert
 * @Author Tang Xiaojiang
 * @Date: 2020/11/21 13:27
 * @Version 1.0
 */
@Mapper
public interface MerchantConvert {
    /**
     * 转化为实例
     */
    MerchantConvert INSTANCE = Mappers.getMapper(MerchantConvert.class);

    /**
     * VO-DTO
     * @param merchantRegisterVO
     * @return
     */
    MerchantDTO vo2DTO(MerchantRegisterVO merchantRegisterVO);

    /**
     * DTO-VO
     * @param merchantDTO
     * @return
     */
    MerchantRegisterVO dto2VO(MerchantDTO merchantDTO);

}
