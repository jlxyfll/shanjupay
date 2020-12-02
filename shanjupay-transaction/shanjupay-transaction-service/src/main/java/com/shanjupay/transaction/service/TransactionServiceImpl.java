package com.shanjupay.transaction.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.AmountUtil;
import com.shanjupay.common.util.EncryptUtil;
import com.shanjupay.common.util.PaymentUtil;
import com.shanjupay.merchant.api.AppService;
import com.shanjupay.merchant.api.MerchantService;
import com.shanjupay.paymentagent.api.PayChannelAgentService;
import com.shanjupay.paymentagent.api.conf.AliConfigParam;
import com.shanjupay.paymentagent.api.conf.WXConfigParam;
import com.shanjupay.paymentagent.api.dto.AlipayBean;
import com.shanjupay.paymentagent.api.dto.PaymentResponseDTO;
import com.shanjupay.paymentagent.api.dto.WeChatBean;
import com.shanjupay.transaction.api.PayChannelService;
import com.shanjupay.transaction.api.TransactionService;
import com.shanjupay.transaction.api.dto.PayChannelParamDTO;
import com.shanjupay.transaction.api.dto.PayOrderDTO;
import com.shanjupay.transaction.api.dto.QrCodeDTO;
import com.shanjupay.transaction.convert.PayOrderConvert;
import com.shanjupay.transaction.entity.PayOrder;
import com.shanjupay.transaction.mapper.PayOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 * @title: TransactionServiceImpl
 * @Author Tang Xiaojiang
 * @Date: 2020/11/27 16:29
 * @Version 1.0
 */
@Slf4j
@Service
public class TransactionServiceImpl implements TransactionService {
    @Value("${shanjupay.payurl}")
    private String payUrl;

    @Reference
    private MerchantService merchantService;

    @Reference
    private AppService appService;

    @Autowired
    private PayOrderMapper payOrderMapper;

    @Reference
    private PayChannelAgentService payChannelAgentService;
    @Autowired
    private PayChannelService payChannelService;

    @Value("${weixin.oauth2RequestUrl}")
    private String oauth2RequestUrl;

    @Value("${weixin.oauth2CodeReturnUrl}")
    private String wxOAuth2CodeReturnUrl;

    @Value("${weixin.oauth2Token}")
    private String oauth2Token;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 生成门店二维码
     *
     * @param qrCodeDTO 传入merchantId,appId、storeid、channel、subject、body
     * @return 支付入口URL，将二维码的参数组成json并用base64编码
     * @throws BusinessException
     */
    @Override
    public String createStoreQrCode(QrCodeDTO qrCodeDTO) throws BusinessException {
        if (qrCodeDTO == null
                || qrCodeDTO.getAppId() == null
                || qrCodeDTO.getMerchantId() == null
                || qrCodeDTO.getStoreId() == null) {
            throw new BusinessException(CommonErrorCode.E_300009);
        }
        // 校验该应用是否属于该商户
        Boolean isAppInMerchant = appService.queryAppInMerchant(qrCodeDTO.getAppId(), qrCodeDTO.getMerchantId());
        if (!isAppInMerchant) {
            throw new BusinessException(CommonErrorCode.E_200005);
        }
        // 校验该门店是否属于该商户
        Boolean isStoreInMerchant = merchantService.queryStoreInMerchant(qrCodeDTO.getStoreId(), qrCodeDTO.getMerchantId());
        if (!isStoreInMerchant) {
            throw new BusinessException(CommonErrorCode.E_200006);
        }
        // 生成支付信息
        PayOrderDTO payOrderDTO = new PayOrderDTO();
        payOrderDTO.setMerchantId(qrCodeDTO.getMerchantId());
        payOrderDTO.setStoreId(qrCodeDTO.getStoreId());
        payOrderDTO.setAppId(qrCodeDTO.getAppId());
        payOrderDTO.setSubject(qrCodeDTO.getSubject());
        payOrderDTO.setChannel("shanju_c2b");
        payOrderDTO.setBody(qrCodeDTO.getBody());
        String jsonString = JSON.toJSONString(payOrderDTO);
        log.info("transaction service createStoreQRCode,JsonString is:{}", jsonString);
        // 将支付信息保存到票据中
//        String ticket = EncryptUtil.encodeUTF8StringBase64(jsonString);
        String ticket = EncryptUtil.encodeUTF8StringBase64(jsonString);
        // 支付入口
        String payEntryUrl = payUrl + ticket;
        log.info("transaction service createStoreQRCode,pay‐entry is:{}", payEntryUrl);
        return payEntryUrl;
    }

    /**
     * 支付宝订单保存,1、保存订单到闪聚平台，2、调用支付渠道代理服务调用支付宝的接口口
     *
     * @param payOrderDTO
     * @return
     * @throws BusinessException
     */
    @Override
    public PaymentResponseDTO submitOrderByAli(PayOrderDTO payOrderDTO) throws BusinessException {
        payOrderDTO.setPayChannel("ALIPAY_WAP");
        // 保存订单
        PayOrderDTO save = save(payOrderDTO);

        // 调用支付代理服务请求第三方支付系统
        PaymentResponseDTO paymentResponseDTO = alipayH5(save.getTradeNo());
        return paymentResponseDTO;
    }

    /**
     * 根据订单号查询订单信息
     *
     * @param tradeNo
     * @return
     * @throws BusinessException
     */
    @Override
    public PayOrderDTO queryPayOrder(String tradeNo) throws BusinessException {
        PayOrder payOrder = payOrderMapper.selectOne(new LambdaQueryWrapper<PayOrder>().eq(PayOrder::getTradeNo, tradeNo));

        return PayOrderConvert.INSTANCE.entity2dto(payOrder);
    }


    /**
     * 保存订单到闪聚平台
     *
     * @param payOrderDTO
     * @return
     * @throws BusinessException
     */
    private PayOrderDTO save(PayOrderDTO payOrderDTO) throws BusinessException {
        PayOrder entity = PayOrderConvert.INSTANCE.dto2entity(payOrderDTO);
        entity.setTradeNo(PaymentUtil.genUniquePayOrderNo());
        // 订单创建时间
        entity.setCreateTime(LocalDateTime.now());
        // 设置过期时间
        entity.setExpireTime(LocalDateTime.now().plus(30, ChronoUnit.MINUTES));
        entity.setCurrency("CNY");
        entity.setTradeState("0");

        payOrderMapper.insert(entity);

        PayOrderDTO payOrderDTONew = PayOrderConvert.INSTANCE.entity2dto(entity);
        return payOrderDTONew;
    }

    /**
     * 调用支付宝下单接口
     *
     * @param tradeNo
     * @return
     */
    private PaymentResponseDTO alipayH5(String tradeNo) {
        // 构建支付实体
        AlipayBean alipayBean = new AlipayBean();

        // 根据订单号查询订单详情
        PayOrderDTO payOrderDTO = queryPayOrder(tradeNo);
        alipayBean.setOutTradeNo(tradeNo);
        alipayBean.setSubject(payOrderDTO.getSubject());
        String totalAmount = null;
        try {
            totalAmount = AmountUtil.changeF2Y(payOrderDTO.getTotalAmount().toString());
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(CommonErrorCode.E_300006);
        }
        alipayBean.setTotalAmount(totalAmount);
        alipayBean.setBody(payOrderDTO.getBody());
        alipayBean.setStoreId(payOrderDTO.getStoreId());
        alipayBean.setExpireTime("30m");

        // 根据应用、服务类型、支付渠道查询支付渠道参数
        PayChannelParamDTO payChannelParamDTO = payChannelService.queryParamByAppPlatformAndPayChannel(payOrderDTO.getAppId(), payOrderDTO.getChannel(), "ALIPAY_WAP");
        if (payChannelParamDTO == null) {
            throw new BusinessException(CommonErrorCode.E_300007);
        }
        // 支付渠道参数
        AliConfigParam aliConfigParam = JSON.parseObject(payChannelParamDTO.getParam(), AliConfigParam.class);

        // 字符编码
        aliConfigParam.setCharest("utf-8");
        PaymentResponseDTO payOrderByAliWAP = payChannelAgentService.createPayOrderByAliWAP(aliConfigParam, alipayBean);
        log.info("支付宝H5支付响应Content:{}", payOrderByAliWAP.getContent());
        return payOrderByAliWAP;
    }

    /**
     * 更新订单支付状态
     *
     * @param tradeNo           闪聚平台订单号
     * @param payChannelTradeNo 支付宝或微信的交易流水号
     * @param state             订单状态 交易状态支付状态,0‐订单生成,1‐支付中(目前未使用),2‐支付成功,4‐关闭 5‐‐失败
     * @throws BusinessException
     */
    @Override
    public void updateOrderTradeNoAndTradeState(String tradeNo, String payChannelTradeNo, String state) {
        LambdaUpdateWrapper<PayOrder> updateWrapper = new LambdaUpdateWrapper<PayOrder>()
                .eq(PayOrder::getTradeNo, tradeNo)
                .set(PayOrder::getPayChannelTradeNo, payChannelTradeNo)
                .set(PayOrder::getTradeState, state);
        if (state != null && "2".equals(state)) {
            updateWrapper.set(PayOrder::getPaySuccessTime, LocalDateTime.now());
        }
        payOrderMapper.update(null, updateWrapper);
    }

    /**
     * 获取微信授权码
     *
     * @param payOrderDTO 订单对象
     * @return
     * @throws BusinessException
     */
    @Override
    public String getWXOAuth2Code(PayOrderDTO payOrderDTO) throws BusinessException {
        // 应用id
        String appId = payOrderDTO.getAppId();
        // 服务类型
        String channel = payOrderDTO.getChannel();
        // 将订单信息封装到state参数中
        String state = EncryptUtil.encodeUTF8StringBase64(JSON.toJSONString(payOrderDTO));
        // 获取微信支付渠道参数，根据应用、服务类型、支付渠道查询支付渠道参数
        PayChannelParamDTO payChannelParamDTO = payChannelService.queryParamByAppPlatformAndPayChannel(appId, channel, "WX_JSAPI");
        if (payChannelParamDTO == null) {
            throw new BusinessException(CommonErrorCode.E_300007);
        }
        // 支付渠道参数
        String payChannelParamDTOParam = payChannelParamDTO.getParam();
        WXConfigParam wxConfigParam = JSON.parseObject(payChannelParamDTOParam, WXConfigParam.class);
        try {
            String url = String.format("%s?appid=%s&scope=snsapi_base&state=%s&redirect_uri=%s"
                    , wxOAuth2CodeReturnUrl, wxConfigParam.getAppId(), state
                    , URLEncoder.encode(wxOAuth2CodeReturnUrl, "utf-8"));
            log.info("微信生成授权码url:{}", url);
            return "redirect:" + url;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            // 生成获取授权码链接失败
            return "forward:/pay-page-error";
        }
    }

    /**
     * 获取微信openId
     *
     * @param code  授权id
     * @param appId 应用id，用于获取微信支付的参数
     * @return
     * @throws BusinessException
     */
    @Override
    public String getWXOAuthOpenId(String code, String appId) throws BusinessException {
        // 获取微信支付渠道参数，根据应用、服务类型、支付渠道查询支付渠道参数
        PayChannelParamDTO payChannelParamDTO = payChannelService.queryParamByAppPlatformAndPayChannel(appId, "shanju_c2b", "WX_JSAPI");
        if (payChannelParamDTO == null) {
            throw new BusinessException(CommonErrorCode.E_300007);
        }
        // 支付渠道参数
        String payChannelParamDTOParam = payChannelParamDTO.getParam();
        WXConfigParam wxConfigParam = JSON.parseObject(payChannelParamDTOParam, WXConfigParam.class);
        // 密钥
        String appSecret = wxConfigParam.getAppSecret();
        // 获取openid地址
        String url = String.format("%s?appid=%s&secret=%s&code=%s&grant_type=authorization_code", oauth2Token, wxConfigParam.getAppId()
                , wxConfigParam.getAppSecret(), code);
        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
        String response = exchange.getBody();
        return JSONObject.parseObject(response).getString("openid");
    }

    /**
     * 微信确认支付
     *
     * @param payOrderDTO
     * @return 微信下单接口的响应数据
     * @throws BusinessException
     */
    @Override
    public Map<String, String> submitOrderByWechat(PayOrderDTO payOrderDTO) throws BusinessException {
        // 微信openid
        String openId = payOrderDTO.getOpenId();
        payOrderDTO.setPayChannel("WX_JSAPI");
        // 保存订单
        PayOrderDTO save = save(payOrderDTO);
        String tradeNo = save.getTradeNo();
        // 微信统一下单
        return weChatJsapi(openId, tradeNo);
    }

    /**
     * 微信jsapi 调用支付渠道代理
     *
     * @param openId
     * @param tradeNo
     * @return
     */
    private Map<String, String> weChatJsapi(String openId, String tradeNo) {
        // 根据订单号查询订单详情
        PayOrderDTO payOrderDTO = queryPayOrder(tradeNo);
        if (payOrderDTO == null) {
            throw new BusinessException(CommonErrorCode.E_400002);
        }
        // 构造微信订单参数实体
        WeChatBean weChatBean = new WeChatBean();
        weChatBean.setOpenId(openId);//openid
        weChatBean.setSpbillCreateIp(payOrderDTO.getClientIp());//客户ip
        weChatBean.setTotalFee(payOrderDTO.getTotalAmount());//金额
        weChatBean.setBody(payOrderDTO.getBody());//订单描述
        weChatBean.setOutTradeNo(payOrderDTO.getTradeNo());//使用聚合平台的订单号 tradeNo
        weChatBean.setNotifyUrl("none");//异步接收微信通知支付结果的地址(暂时不用)3
        // 根据应用、服务类型、支付渠道查询支付渠道参数
        PayChannelParamDTO payChannelParamDTO = payChannelService.queryParamByAppPlatformAndPayChannel(payOrderDTO.getAppId(), "shanju_c2b", "WX_JSAPI");
        if (payChannelParamDTO == null) {
            throw new BusinessException(CommonErrorCode.E_300007);
        }
        // 支付宝渠道参数
        WXConfigParam wxConfigParam = JSON.parseObject(payChannelParamDTO.getParam(), WXConfigParam.class);
        Map<String, String> payOrderByWeChatJSAPI = payChannelAgentService.createPayOrderByWeChatJSAPI(wxConfigParam, weChatBean);
        return payOrderByWeChatJSAPI;
    }
}
