package com.shanjupay.merchant.convert;

import com.shanjupay.merchant.api.dto.StaffDTO;
import com.shanjupay.merchant.api.dto.StoreDTO;
import com.shanjupay.merchant.entity.Staff;
import com.shanjupay.merchant.entity.Store;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @title: MerchantCovert
 * @Author Tang Xiaojiang
 * @Date: 2020/11/21 13:27
 * @Version 1.0
 */
@Mapper
public interface StaffConvert {
    /**
     * 转化为实例
     */
    StaffConvert INSTANCE = Mappers.getMapper(StaffConvert.class);

    /**
     * entity转为DTO
     *
     * @param staff entity
     * @return
     */
    StaffDTO staff2StaffDTO(Staff staff);

    /**
     * DTO转为entity
     *
     * @param staffDTO DTO
     * @return
     */
    Staff storeDTO2Store(StaffDTO staffDTO);

    /**
     * list转化
     *
     * @param list
     * @return
     */
    List<StaffDTO> staffList2DTO(List<Staff> list);

}
