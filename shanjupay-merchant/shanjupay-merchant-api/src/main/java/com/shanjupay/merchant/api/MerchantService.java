package com.shanjupay.merchant.api;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.api.dto.StaffDTO;
import com.shanjupay.merchant.api.dto.StoreDTO;

/**
 * @title: MerchantService
 * @Author Tang Xiaojiang
 * @Date: 2020/11/20 13:55
 * @Version 1.0
 */
public interface MerchantService {
    /**
     * 根据ID查询详细信息
     *
     * @param merchantId
     * @return
     */
    MerchantDTO queryMerchantById(Long merchantId);

    /**
     * 商户注册接口
     *
     * @param merchantDTO
     * @return
     * @throws BusinessException
     */
    MerchantDTO createMerchant(MerchantDTO merchantDTO) throws BusinessException;

    /**
     * 商户下新增门店
     *
     * @param storeDTO
     * @return
     * @throws BusinessException
     */
    StoreDTO creatStore(StoreDTO storeDTO) throws BusinessException;

    /**
     * 商户新增员工
     *
     * @param staffDTO
     * @return
     * @throws BusinessException
     */
    StaffDTO createStaff(StaffDTO staffDTO) throws BusinessException;

    /**
     * 为门店设置管理员
     *
     * @param storeId
     * @param staffId
     * @throws BusinessException
     */
    void bindStaffToStore(Long storeId, Long staffId) throws BusinessException;

    /**
     * 查询租户下的商户
     *
     * @param tenantId
     * @return
     * @throws BusinessException
     */
    MerchantDTO queryMerchantByTenantId(Long tenantId) throws BusinessException;


}



