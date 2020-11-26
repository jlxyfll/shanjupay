package com.shanjupay.merchant.convert;

import com.shanjupay.merchant.api.dto.AppDTO;
import com.shanjupay.merchant.api.dto.StoreDTO;
import com.shanjupay.merchant.entity.App;
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
public interface StoreConvert {
    /**
     * 转化为实例
     */
    StoreConvert INSTANCE = Mappers.getMapper(StoreConvert.class);

    /**
     * entity转为DTO
     *
     * @param store entity
     * @return
     */
    StoreDTO store2StoreDTO(Store store);

    /**
     * DTO转为entity
     *
     * @param storeDTO DTO
     * @return
     */
    Store storeDTO2Store(StoreDTO storeDTO);

    /**
     * list转化
     *
     * @param list
     * @return
     */
    List<StoreDTO> storeList2DTO(List<Store> list);

}
