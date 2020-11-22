package com.shanjupay.merchant.convert;

import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.vo.MerchantDetailVO;
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
public interface MerchantDetailConvert {
    /**
     * 转化为实例
     */
    MerchantDetailConvert INSTANCE = Mappers.getMapper(MerchantDetailConvert.class);

    /**
     * VO-DTO
     *
     * @param merchantDetailVO
     * @return
     */
    MerchantDTO vo2DTO(MerchantDetailVO merchantDetailVO);

    /**
     * DTO-VO
     *
     * @param merchantDTO
     * @return
     */
    MerchantDetailVO dto2VO(MerchantDTO merchantDTO);

}
