package com.shanjupay.merchant.api;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.merchant.api.dto.AppDTO;

import java.util.List;

/**
 * @title: AppService
 * @Author Tang Xiaojiang
 * @Date: 2020/11/22 12:05
 * @Version 1.0
 */
public interface AppService {
    /**
     * 商户下创建应用
     *
     * @param merchantId
     * @param appDTO
     * @return
     * @throws BusinessException
     */
    AppDTO createApp(Long merchantId, AppDTO appDTO) throws BusinessException;

    /**
     * 查询商户下的应用列表
     * @param merchantId
     * @return
     * @throws BusinessException
     */
    List<AppDTO> queryAppByMerchant(Long merchantId) throws BusinessException;

    /**
     * 根据业务id查询应用
     * @param appId
     * @return
     * @throws BusinessException
     */
    AppDTO getAppById(String appId) throws BusinessException;
}
