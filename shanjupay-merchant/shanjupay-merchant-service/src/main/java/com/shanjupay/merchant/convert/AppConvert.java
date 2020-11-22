package com.shanjupay.merchant.convert;

import com.shanjupay.merchant.api.dto.AppDTO;
import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.entity.App;
import com.shanjupay.merchant.entity.Merchant;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;

/**
 * @title: MerchantCovert
 * @Author Tang Xiaojiang
 * @Date: 2020/11/21 13:27
 * @Version 1.0
 */
@Mapper
public interface AppConvert {
    /**
     * 转化为实例
     */
    AppConvert INSTANCE = Mappers.getMapper(AppConvert.class);

    /**
     * entity转为DTO
     *
     * @param app entity
     * @return
     */
    AppDTO app2AppDTO(App app);

    /**
     * DTO转为entity
     *
     * @param appDTO DTO
     * @return
     */
    App appDTO2App(AppDTO appDTO);

    /**
     * list转化
     *
     * @param list
     * @return
     */
    List<AppDTO> AppList2DTO(List<App> list);

}
