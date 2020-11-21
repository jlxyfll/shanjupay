package com.shanjupay.merchant.convert;

import com.shanjupay.merchant.api.dto.MerchantDTO;
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
public interface MerchantConvert {
    /**
     * 转化为实例
     */
    MerchantConvert INSTANCE = Mappers.getMapper(MerchantConvert.class);

    /**
     * entity转为DTO
     *
     * @param merchant entity
     * @return
     */
    MerchantDTO merchant2MerchantDTO(Merchant merchant);

    /**
     * DTO转为entity
     *
     * @param merchantDTO DTO
     * @return
     */
    Merchant merchantDTO2Merchant(MerchantDTO merchantDTO);

    /**
     * list转化
     *
     * @param list
     * @return
     */
    List<MerchantDTO> merchantList2DTO(List<Merchant> list);

    /**
     * 测试类型转化
     *
     * @param args
     */
    public static void main(String[] args) {
        // entity转dto
        Merchant merchant = new Merchant();
        merchant.setMobile("12345678");
        merchant.setUsername("测试");
        MerchantDTO merchantDTO = MerchantConvert.INSTANCE.merchant2MerchantDTO(merchant);
        System.out.println(merchantDTO);
        System.out.println("=-=-=-=-=-=-=-=-=-=");
        // dto转entity
        merchantDTO.setMerchantName("商户名称");
        Merchant merchant1 = MerchantConvert.INSTANCE.merchantDTO2Merchant(merchantDTO);
        System.out.println(merchant1);
        System.out.println("=-=-=-=-=-=-=-=-=-=");
        // list转化
        List<Merchant> list = new ArrayList<>();
        list.add(merchant);
        list.add(merchant1);
        List<MerchantDTO> merchantDTOList = MerchantConvert.INSTANCE.merchantList2DTO(list);
        System.out.println(merchantDTOList);
        System.out.println("=-=-=-=-=-=-=-=-=-=");
        for (MerchantDTO dto : merchantDTOList) {
            System.out.println(dto);
            System.out.println("00000000000000");
        }
        System.out.println("=-=-=-=-=-=-=-=-=-=");
    }

}
