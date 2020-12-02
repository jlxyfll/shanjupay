package com.shanjupay.paymentagent.service;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePayModel;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.paymentagent.api.PayChannelAgentService;
import com.shanjupay.paymentagent.api.conf.AliConfigParam;
import com.shanjupay.paymentagent.api.conf.WXConfigParam;
import com.shanjupay.paymentagent.api.conf.WXSDKConfig;
import com.shanjupay.paymentagent.api.dto.AlipayBean;
import com.shanjupay.paymentagent.api.dto.PaymentResponseDTO;
import com.shanjupay.paymentagent.api.dto.TradeStatus;
import com.shanjupay.paymentagent.api.dto.WeChatBean;
import com.shanjupay.paymentagent.common.constant.AliCodeConstants;
import com.shanjupay.paymentagent.message.PayProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.shanjupay.paymentagent.api.dto.PaymentResponseDTO.fail;

/**
 * @title: PayChannelAgentServiceImpl
 * @Author Tang Xiaojiang
 * @Date: 2020/11/28 14:43
 * @Version 1.0
 */
@Service
@Slf4j
public class PayChannelAgentServiceImpl implements PayChannelAgentService {
    @Autowired
    private PayProducer payProducer;

    /**
     * 调用支付宝手机WAP下单接口
     *
     * @param aliConfigParam 支付渠道配置的参数（配置的支付宝的必要参数）
     * @param alipayBean     业务参数（商户订单号，订单标题，订单描述，，）
     * @return
     * @throws BusinessException
     */
    @Override
    public PaymentResponseDTO createPayOrderByAliWAP(AliConfigParam aliConfigParam, AlipayBean alipayBean) throws BusinessException {
        log.info("支付宝请求参数:{}", alipayBean.toString());
        // 支付宝渠道参数
        String gateway = aliConfigParam.getUrl();//支付宝下单接口地址
        String appId = aliConfigParam.getAppId();//appid
        String rsaPrivateKey = aliConfigParam.getRsaPrivateKey();//私钥
        String format = aliConfigParam.getFormat();//数据格式json
        String charest = aliConfigParam.getCharest();//字符编码
        String alipayPublicKey = aliConfigParam.getAlipayPublicKey(); //公钥
        String signtype = aliConfigParam.getSigntype();//签名算法类型
        String notifyUrl = aliConfigParam.getNotifyUrl();//支付结果通知地址
        String returnUrl = aliConfigParam.getReturnUrl();//支付完成返回商户地址

        // 支付宝sdk客户端
        AlipayClient client = new DefaultAlipayClient(gateway, appId, rsaPrivateKey, format, charest, alipayPublicKey, signtype);

        // 封装请求支付信息
        AlipayTradeWapPayRequest alipayTradeWapPayRequest = new AlipayTradeWapPayRequest();
        AlipayTradeWapPayModel alipayTradeWapPayModel = new AlipayTradeWapPayModel();
        alipayTradeWapPayModel.setOutTradeNo(alipayBean.getOutTradeNo());
        alipayTradeWapPayModel.setSubject(alipayBean.getSubject());
        alipayTradeWapPayModel.setTotalAmount(alipayBean.getTotalAmount());
        alipayTradeWapPayModel.setBody(alipayBean.getBody());
        alipayTradeWapPayModel.setProductCode(alipayBean.getProductCode());
        alipayTradeWapPayModel.setTimeoutExpress(alipayBean.getExpireTime());
        alipayTradeWapPayRequest.setBizModel(alipayTradeWapPayModel);
        String jsonString = JSON.toJSONString(alipayBean);
        log.info("createPayOrderByAliWAP..alipayRequest:{}", jsonString);
        // 设置异步通知地址
        alipayTradeWapPayRequest.setNotifyUrl(notifyUrl);
        // 设置同步地址
        alipayTradeWapPayRequest.setReturnUrl(returnUrl);

        try {
            // 发送支付结果查询延迟消息
            PaymentResponseDTO<AliConfigParam> notice = new PaymentResponseDTO<AliConfigParam>();
            notice.setOutTradeNo(alipayBean.getOutTradeNo());
            notice.setContent(aliConfigParam);
            notice.setMsg("ALIPAY_WAP");
            payProducer.payOrderNotice(notice);

            // 调用SDK提交表单
            AlipayTradeWapPayResponse response = client.pageExecute(alipayTradeWapPayRequest);
            log.info("支付宝手机网站支付预支付订单信息" + response);
            PaymentResponseDTO res = new PaymentResponseDTO();
            res.setContent(response.getBody());
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            // 支付宝确认支付失败
            throw new BusinessException(CommonErrorCode.E_400002);
        }
    }

    /**
     * 支付宝交易状态查询
     *
     * @param aliConfigParam 支付渠道参数
     * @param outTradeNo     闪聚平台订单号
     * @return
     * @throws BusinessException
     */
    @Override
    public PaymentResponseDTO queryPayOrderByAli(AliConfigParam aliConfigParam, String outTradeNo) throws BusinessException {
        String gateway = aliConfigParam.getUrl();//支付宝下单接口地址
        String appId = aliConfigParam.getAppId();//appid
        String rsaPrivateKey = aliConfigParam.getRsaPrivateKey();//私钥
        String format = aliConfigParam.getFormat();//数据格式json
        String charest = aliConfigParam.getCharest();//字符编码
        String alipayPublicKey = aliConfigParam.getAlipayPublicKey(); //公钥
        String signtype = aliConfigParam.getSigntype();//签名算法类型
        log.info("C扫B请求支付宝查询订单，参数：{}", JSON.toJSONString(aliConfigParam));

        // 支付宝sdk客户端
        AlipayClient client = new DefaultAlipayClient(gateway, appId, rsaPrivateKey, format, charest, alipayPublicKey, signtype);
        AlipayTradeQueryRequest alipayTradeQueryRequest = new AlipayTradeQueryRequest();
        AlipayTradePayModel alipayTradePayModel = new AlipayTradePayModel();
        // 闪聚平台订单号
        alipayTradePayModel.setOutTradeNo(outTradeNo);
        // 封装请求参数
        alipayTradeQueryRequest.setBizModel(alipayTradePayModel);
        PaymentResponseDTO<Object> dto = null;
        try {
            // 请求支付宝接口
            AlipayTradeQueryResponse queryResponse = client.execute(alipayTradeQueryRequest);
            // 接口调用成功
            if (AliCodeConstants.SUCCESSCODE.equals(queryResponse.getCode())) {
                // 将支付宝响应的状态转换为闪聚平台的状态
                TradeStatus tradeStatus = covertAliTradeStatusToShanjuCode(queryResponse.getTradeStatus());
                dto = PaymentResponseDTO.success(queryResponse.getTradeNo(), queryResponse.getOutTradeNo(), tradeStatus, queryResponse.getMsg() + " " + queryResponse.getSubMsg());
                log.info("‐‐‐‐查询支付宝H5支付结果:{}", JSON.toJSONString(dto));
                return dto;
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
            log.warn(e.getMessage(), e);
        }
        dto = fail("查询支付宝支付结果异常", outTradeNo, TradeStatus.UNKNOWN);
        return dto;
    }


    /**
     * 将支付宝查询时订单状态trade_status 转换为 闪聚订单状态
     *
     * @param aliTradeStatus 支付宝交易状态
     *                       WAIT_BUYER_PAY（交易创建，等待买家付款）
     *                       TRADE_CLOSED（未付款交易超时关闭，或支付完成后全额退款）
     *                       TRADE_SUCCESS（交易支付成功）
     *                       TRADE_FINISHED（交易结束，不可退款）
     * @return
     */
    private TradeStatus covertAliTradeStatusToShanjuCode(String aliTradeStatus) {
        switch (aliTradeStatus) {
            case AliCodeConstants.WAIT_BUYER_PAY:
                return TradeStatus.USERPAYING;
            case AliCodeConstants.TRADE_SUCCESS:
            case AliCodeConstants.TRADE_FINISHED:
                return TradeStatus.SUCCESS;
            default:
                return TradeStatus.FAILED;
        }
    }

    /**
     * 微信jsapi下单接口请求
     *
     * @param wxConfigParam
     * @param weChatBean
     * @return
     * @throws BusinessException
     */
    @Override
    public Map<String, String> createPayOrderByWeChatJSAPI(WXConfigParam wxConfigParam, WeChatBean weChatBean) throws BusinessException {
        // 通过实际支付参数匹配
        WXSDKConfig config = new WXSDKConfig(wxConfigParam);
        try {
            // 发送支付结果查询延迟消息
            PaymentResponseDTO<WXConfigParam> notice = new PaymentResponseDTO<WXConfigParam>();
            notice.setOutTradeNo(weChatBean.getOutTradeNo());
            notice.setContent(wxConfigParam);
            notice.setMsg("WX_JSAPI");
            payProducer.payOrderNotice(notice);
            WXPay wxPay = new WXPay(config);
            // 按照微信统一下单接口要求构造请求参数
            Map<String, String> requestParam = new HashMap<String, String>();
            requestParam.put("body", weChatBean.getBody());
            requestParam.put("out_trade_no", weChatBean.getOutTradeNo());
            requestParam.put("fee_type", "CNY");
            requestParam.put("total_fee", String.valueOf(weChatBean.getTotalFee()));
            requestParam.put("spbill_create_ip", weChatBean.getSpbillCreateIp());
            requestParam.put("notify_url", weChatBean.getNotifyUrl());
            requestParam.put("trade_type", "JSAPI");
            requestParam.put("openid", weChatBean.getOpenId());
            // 调用微信统一下单API
            Map<String, String> resp = wxPay.unifiedOrder(requestParam);
            // 返回h5网页需要的数据
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
            String key = wxConfigParam.getKey();
            Map<String, String> jsapiPayParam = new HashMap<>();
            jsapiPayParam.put("appId", resp.get("appid"));
            jsapiPayParam.put("package", "prepay_id=" + resp.get("prepay_id"));
            jsapiPayParam.put("timeStamp", timestamp);
            jsapiPayParam.put("nonceStr", UUID.randomUUID().toString());
            jsapiPayParam.put("signType", "HMAC‐SHA256");
            jsapiPayParam.put("paySign", WXPayUtil.generateSignature(jsapiPayParam, key, WXPayConstants.SignType.HMACSHA256));
            log.info("微信JSAPI支付响应内容：{}", jsapiPayParam);
            return jsapiPayParam;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(CommonErrorCode.E_400001);
        }
    }

    /**
     * 查询微信支付结果
     *
     * @param wxConfigParam
     * @param outTradeNo
     * @return
     */
    @Override
    public PaymentResponseDTO queryPayOrderByWeChat(WXConfigParam wxConfigParam, String outTradeNo) {
        WXSDKConfig config = new WXSDKConfig(wxConfigParam);
        Map<String, String> resp = null;
        try {
            WXPay wxPay = new WXPay(config);
            Map<String, String> data = new HashMap<String, String>();
            data.put("out_trade_no", outTradeNo);
            resp = wxPay.orderQuery(data);
        } catch (Exception e) {
            e.printStackTrace();
            log.warn(e.getMessage(), e);
            return PaymentResponseDTO.fail("调用微信查询订单异常", outTradeNo, TradeStatus.UNKNOWN);
        }

        String returnCode = resp.get("return_code");
        String resultCode = resp.get("result_code");
        String tradeState = resp.get("trade_state");
        String transactionId = resp.get("transaction_id");
        String tradeType = resp.get("trade_type");
        String returnMsg = resp.get("return_msg");

        if ("SUCCESS".equals(returnCode) && "SUCCESS".equals(resultCode)) {
            if ("SUCCESS".equals(tradeState)) {
                return PaymentResponseDTO.success(transactionId, outTradeNo, TradeStatus.SUCCESS, "");
            } else if ("USERPAYING".equals(tradeState)) {
                return PaymentResponseDTO.success(transactionId, outTradeNo, TradeStatus.USERPAYING, "");
            } else if ("PAYERROR".equals(tradeState)) {
                return PaymentResponseDTO.success(transactionId, outTradeNo, TradeStatus.FAILED, returnMsg);
            } else if ("CLOSED".equals(tradeState)) {
                return PaymentResponseDTO.success(transactionId, outTradeNo, TradeStatus.REVOKED, returnMsg);
            }
        }
        PaymentResponseDTO<Object> responseDTO = PaymentResponseDTO.success("暂不支持其他状态", transactionId, outTradeNo, TradeStatus.UNKNOWN);

        return responseDTO;
    }
}
