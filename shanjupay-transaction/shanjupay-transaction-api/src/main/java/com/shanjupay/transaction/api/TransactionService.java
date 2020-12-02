package com.shanjupay.transaction.api;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.paymentagent.api.dto.PaymentResponseDTO;
import com.shanjupay.transaction.api.dto.PayOrderDTO;
import com.shanjupay.transaction.api.dto.QrCodeDTO;

import java.util.Map;

/**
 * @title: TransactionService
 * @Author Tang Xiaojiang
 * @Date: 2020/11/27 16:27
 * @Version 1.0
 */
public interface TransactionService {
    /**
     * 生成门店二维码
     *
     * @param qrCodeDTO 传入merchantId,appId、storeid、channel、subject、body
     * @return 支付入口URL，将二维码的参数组成json并用base64编码
     * @throws BusinessException
     */
    String createStoreQrCode(QrCodeDTO qrCodeDTO) throws BusinessException;

    /**
     * 支付宝订单保存,1、保存订单到闪聚平台，2、调用支付渠道代理服务调用支付宝的接口口
     *
     * @param payOrderDTO
     * @return
     * @throws BusinessException
     */
    PaymentResponseDTO submitOrderByAli(PayOrderDTO payOrderDTO) throws BusinessException;

    /**
     * 根据订单号查询订单信息
     *
     * @param tradeNo
     * @return
     * @throws BusinessException
     */
    PayOrderDTO queryPayOrder(String tradeNo) throws BusinessException;

    /**
     * 更新订单支付状态
     *
     * @param tradeNo           闪聚平台订单号
     * @param payChannelTradeNo 支付宝或微信的交易流水号
     * @param state             订单状态 交易状态支付状态,0‐订单生成,1‐支付中(目前未使用),2‐支付成功,4‐关闭 5‐‐失败
     * @throws BusinessException
     */
    void updateOrderTradeNoAndTradeState(String tradeNo, String payChannelTradeNo, String state) throws BusinessException;

    /**
     * 获取微信授权码
     *
     * @param payOrderDTO 订单对象
     * @return
     * @throws BusinessException
     */
    String getWXOAuth2Code(PayOrderDTO payOrderDTO) throws BusinessException;

    /**
     * 获取微信openId
     *
     * @param code  授权id
     * @param appId 应用id，用于获取微信支付的参数
     * @return
     * @throws BusinessException
     */
    String getWXOAuthOpenId(String code, String appId) throws BusinessException;

    /**
     * 微信确认支付
     * @param payOrderDTO
     * @return 微信下单接口的响应数据
     * @throws BusinessException
     */
    Map<String, String> submitOrderByWechat(PayOrderDTO payOrderDTO) throws BusinessException;
}
