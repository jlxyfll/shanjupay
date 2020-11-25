package com.shanjupay.transaction.api;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.transaction.api.dto.PayChannelDTO;
import com.shanjupay.transaction.api.dto.PayChannelParamDTO;
import com.shanjupay.transaction.api.dto.PlatformChannelDTO;

import java.util.List;

/**
 * @title: PayChannelService
 * @Author Tang Xiaojiang
 * @Date: 2020/11/22 15:30
 * @Version 1.0
 * 支付渠道服务 管理平台支付渠道，原始支付渠道，以及相关配置
 */
public interface PayChannelService {
    /**
     * 获取平台服务类型
     *
     * @return
     * @throws BusinessException
     */
    List<PlatformChannelDTO> queryPlatformChannel() throws BusinessException;

    /**
     * 为app绑定平台服务类型
     *
     * @param appId
     * @param platformChannelCodes 平台服务类型列表
     * @throws BusinessException
     */
    void bindPlatformChannelForApp(String appId, String platformChannelCodes) throws BusinessException;

    /**
     * 应用是否已经绑定了某个服务类型
     *
     * @param appId
     * @param platformChannel
     * @return 已绑定返回1，否则 返回0
     * @throws BusinessException
     */
    int queryAppBindPlatformChannel(String appId, String platformChannel) throws
            BusinessException;

    /**
     * 根据平台服务类型获取支付渠道列表
     *
     * @param platformChannel
     * @return
     * @throws BusinessException
     */
    List<PayChannelDTO> queryPayChannelByPlatformChannel(String platformChannel) throws BusinessException;

    /**
     * 保存支付渠道参数
     *
     * @param payChannelParam 商户原始支付渠道参数
     * @throws BusinessException
     */
    void savePayChannelParam(PayChannelParamDTO payChannelParam) throws BusinessException;

    /**
     * 获取指定应用指定服务类型下所包含的原始支付渠道参数列表
     *
     * @param appId
     * @param platformChannel
     * @return
     * @throws BusinessException
     */
    List<PayChannelParamDTO> queryPayChannelParamByAppAndPlatform(String appId, String platformChannel) throws BusinessException;

    /**
     * 获取指定应用指定服务类型下所包含的某个原始支付参数
     *
     * @param appId
     * @param platformChannel
     * @param payChannel
     * @return
     * @throws BusinessException
     */
    PayChannelParamDTO queryParamByAppPlatformAndPayChannel(String appId, String platformChannel, String payChannel) throws BusinessException;
}
