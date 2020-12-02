package com.shanjupay.paymentagent.api;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.paymentagent.api.conf.AliConfigParam;
import com.shanjupay.paymentagent.api.conf.WXConfigParam;
import com.shanjupay.paymentagent.api.dto.AlipayBean;
import com.shanjupay.paymentagent.api.dto.PaymentResponseDTO;
import com.shanjupay.paymentagent.api.dto.WeChatBean;

import java.util.Map;

/**
 * @title: PayChannelAgentService
 * @Author Tang Xiaojiang
 * @Date: 2020/11/28 14:32
 * @Version 1.0
 */
public interface PayChannelAgentService {
    /**
     * 调用支付宝手机WAP下单接口
     *
     * @param aliConfigParam 支付渠道配置的参数（配置的支付宝的必要参数）
     * @param alipayBean     业务参数（商户订单号，订单标题，订单描述，，）
     * @return 统一返回PaymentResponseord
     * @throws BusinessException
     */
    PaymentResponseDTO createPayOrderByAliWAP(AliConfigParam aliConfigParam, AlipayBean alipayBean) throws BusinessException;

    /**
     * 支付宝交易状态查询
     *
     * @param aliConfigParam 支付渠道参数
     * @param outTradeNo     闪聚平台订单号
     * @return
     * @throws BusinessException
     */
    PaymentResponseDTO queryPayOrderByAli(AliConfigParam aliConfigParam, String outTradeNo) throws BusinessException;

    /**
     * 微信jsapi下单接口请求
     *
     * @param wxConfigParam
     * @param weChatBean
     * @return
     * @throws BusinessException
     */
    Map<String, String> createPayOrderByWeChatJSAPI(WXConfigParam wxConfigParam, WeChatBean weChatBean) throws BusinessException;

    /**
     * 查询微信支付结果
     * @param wxConfigParam
     * @param outTradeNo
     * @return
     */
    PaymentResponseDTO queryPayOrderByWeChat(WXConfigParam wxConfigParam, String outTradeNo);
}
