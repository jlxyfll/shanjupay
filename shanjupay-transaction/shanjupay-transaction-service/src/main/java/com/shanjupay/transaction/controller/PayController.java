package com.shanjupay.transaction.controller;

import com.alibaba.fastjson.JSON;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.*;
import com.shanjupay.merchant.api.AppService;
import com.shanjupay.merchant.api.dto.AppDTO;
import com.shanjupay.paymentagent.api.conf.WXConfigParam;
import com.shanjupay.paymentagent.api.dto.PaymentResponseDTO;
import com.shanjupay.transaction.api.PayChannelService;
import com.shanjupay.transaction.api.TransactionService;
import com.shanjupay.transaction.api.dto.PayChannelParamDTO;
import com.shanjupay.transaction.api.dto.PayOrderDTO;
import com.shanjupay.transaction.convert.PayOrderConvert;
import com.shanjupay.transaction.vo.OrderConfirmVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * @title: PayController
 * @Author Tang Xiaojiang
 * @Date: 2020/11/28 10:28
 * @Version 1.0
 */
@Controller
@Slf4j
public class PayController {
    @Autowired
    private TransactionService transactionService;

    @Reference
    private AppService appService;


    /**
     * 支付入口
     *
     * @param ticket  传入数据，对json数据进行base64编码
     * @param request
     * @return
     */
    @RequestMapping(value = "/pay-entry/{ticket}")
    public String payEntry(@PathVariable("ticket") String ticket, HttpServletRequest request) throws Exception {
        try {
            // 1、准备确认页面所需要的数据
            String jsonString = EncryptUtil.decodeUTF8StringBase64(ticket);
            // 将json串转成对象
            PayOrderDTO payOrderDTO = JSON.parseObject(jsonString, PayOrderDTO.class);
            // 将对象的属性和值组成一个url的key/value串
            String params = ParseURLPairUtil.parseURLPair(payOrderDTO);
            // 2、解析客户端的类型（微信、支付宝）
            BrowserType browserType = BrowserType.valueOfUserAgent(request.getHeader("user-agent"));
            switch (browserType) {
                case ALIPAY:
                    return "forward:/pay-page?" + params;
                case WECHAT:
//                    return "forward:/pay-page?" + params;
                    return transactionService.getWXOAuth2Code(payOrderDTO);
                default:
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
        }
        return "forward:/pay-page-error";
    }

    /**
     * 支付宝下单接口,将前端订单确认页面,点击确认支付请求进来
     *
     * @param orderConfirmVO 订单信息
     * @param request
     * @param response
     */
    @ApiOperation("支付宝门店下单付款")
    @PostMapping("/createAliPayOrder")
    public void createAlipayOrderForStore(OrderConfirmVO orderConfirmVO, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (StringUtils.isBlank(orderConfirmVO.getAppId())) {
            throw new BusinessException(CommonErrorCode.E_300003);
        }

        PayOrderDTO payOrderDTO = PayOrderConvert.INSTANCE.vo2dto(orderConfirmVO);
        payOrderDTO.setTotalAmount(Integer.valueOf(AmountUtil.changeY2F(orderConfirmVO.getTotalAmount())));
        payOrderDTO.setClientIp(IPUtil.getIpAddr(request));
        // 获取下单应用信息
        AppDTO app = appService.getAppById(payOrderDTO.getAppId());
        // 设置所属商户
        payOrderDTO.setMerchantId(app.getMerchantId());
        PaymentResponseDTO paymentResponseDTO = transactionService.submitOrderByAli(payOrderDTO);
        String content = String.valueOf(paymentResponseDTO.getContent());
        log.info("支付宝H5支付响应的结果：{}", content);
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(content);
        response.getWriter().flush();
        response.getWriter().close();
    }

    /**
     * 微信授权码回调
     *
     * @param code
     * @param state
     * @return
     */
    @ApiOperation("微信授权码回调")
    @GetMapping("/wx-oauth-code-return")
    public String wxOAuth2CodeReturn(@RequestParam String code, @RequestParam String state) {
        // 将之前state中保存的订单信息读取出来
        PayOrderDTO payOrderDTO = JSON.parseObject(EncryptUtil.decodeUTF8StringBase64(state), PayOrderDTO.class);
        // 应用id
        String appId = payOrderDTO.getAppId();
        // 获取openId
        String openId = transactionService.getWXOAuthOpenId(code, appId);
        try {
            // 将订单信息转成query参数的形式拼接起来
            String orderInfo = ParseURLPairUtil.parseURLPair(payOrderDTO);
            return String.format("forward:/pay‐page?openId=%s&%s", openId, orderInfo);
        } catch (Exception e) {
            e.printStackTrace();
            return "forward:/pay-page-error";
        }
    }

    /**
     * 微信确认支付
     *
     * @param orderConfirmVO
     * @param request
     * @return
     */
    @ApiOperation("微信门店下单付款")
    @PostMapping("/wxjspay")
    public ModelAndView createWXOrderForStore(OrderConfirmVO orderConfirmVO, HttpServletRequest request) {
        if (StringUtils.isBlank(orderConfirmVO.getOpenId())) {
            throw new BusinessException(CommonErrorCode.E_300002);
        }
        PayOrderDTO payOrderDTO = PayOrderConvert.INSTANCE.vo2dto(orderConfirmVO);
        // 应用id
        String appId = payOrderDTO.getAppId();
        AppDTO app = appService.getAppById(appId);
        payOrderDTO.setMerchantId(app.getMerchantId());
        payOrderDTO.setClientIp(IPUtil.getIpAddr(request));
        // 将前端输入的元转分
        payOrderDTO.setTotalAmount(Integer.parseInt(AmountUtil.changeY2F(orderConfirmVO.getTotalAmount().toString())));
        // 调用微信下单接口
        Map<String, String> jsapiResponse = transactionService.submitOrderByWechat(payOrderDTO);
        log.info("/wxjspay 微信门店下单接口响应内容：{}", jsapiResponse);
        return new ModelAndView("wapay", jsapiResponse);
    }


}
