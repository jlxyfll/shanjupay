package com.shanjupay.merchant.controller;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.merchant.util.SecurityUtil;
import com.shanjupay.transaction.api.PayChannelService;
import com.shanjupay.transaction.api.dto.PayChannelDTO;
import com.shanjupay.transaction.api.dto.PayChannelParamDTO;
import com.shanjupay.transaction.api.dto.PlatformChannelDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @title: PlatformParamController
 * @Author Tang Xiaojiang
 * @Date: 2020/11/22 15:38
 * @Version 1.0
 */
@Api(value = "商户平台‐渠道和支付参数相关", tags = "商户平台‐渠道和支付参数", description = "商户平台‐渠道和支付参数相关")
@RestController
@Slf4j
public class PlatformParamController {
    @Reference
    private PayChannelService payChannelService;

    @ApiOperation("获取平台服务类型")
    @GetMapping(value = "/my/platform‐channels")
    public List<PlatformChannelDTO> queryPlatformChannel() {
        List<PlatformChannelDTO> platformChannelDTOList = payChannelService.queryPlatformChannel();
        return platformChannelDTOList;
    }

    @ApiOperation("根据平台服务类型获取支付渠道列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "platformChannelCode", value = "服务类型编码", required = true, dataType = "String", paramType = "path")})
    @GetMapping(value = "/my/pay‐channels/platform‐channel/{platformChannel}")
    public List<PayChannelDTO> queryPayChannelByPlatformChannel(@PathVariable("platformChannel") String platformChannel) {
        List<PayChannelDTO> payChannelDTOList = payChannelService.queryPayChannelByPlatformChannel(platformChannel);
        return payChannelDTOList;
    }

    @ApiOperation("商户配置支付渠道参数")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "payChannelParam", value = "商户配置支付渠道参数", required =
                    true, dataType = "PayChannelParamDTO", paramType = "body")
    })
    @RequestMapping(value = "/my/pay-channel-params", method = {RequestMethod.POST, RequestMethod.PUT})
    public void createPayChannelParam(@RequestBody PayChannelParamDTO payChannelParam) {

        if (payChannelParam == null || payChannelParam.getChannelName() == null) {
            throw new BusinessException(CommonErrorCode.E_300009);
        }
        // 商户ID
        Long merchantId = SecurityUtil.getMerchantId();
        payChannelParam.setMerchantId(merchantId);
        payChannelService.savePayChannelParam(payChannelParam);
    }

    @ApiOperation("获取指定应用指定服务类型下所包含的原始支付渠道参数列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "appId", value = "应用id", required = true, dataType =
                    "String", paramType = "path"),
            @ApiImplicitParam(name = "platformChannel", value = "服务类型", required = true,
                    dataType = "String", paramType = "path")})
    @GetMapping(value = "/my/pay‐channel‐params/apps/{appId}/platformchannels/{platformChannel}")
    public List<PayChannelParamDTO> queryPayChannelParam(@PathVariable("appId") String appId, @PathVariable("platformChannel") String platformChannel) {
        List<PayChannelParamDTO> payChannelParamDTOList = payChannelService.queryPayChannelParamByAppAndPlatform(appId, platformChannel);
        return payChannelParamDTOList;
    }

    @ApiOperation("根据应用和服务类型和支付渠道获取单个支付渠道参数")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "appId", value = "应用id", required = true, dataType = "String",
                    paramType = "path"),
            @ApiImplicitParam(name = "platformChannel", value = "平台支付渠道编码", required = true,
                    dataType = "String", paramType = "path"),
            @ApiImplicitParam(name = "payChannel", value = "实际支付渠道编码", required = true,
                    dataType = "String", paramType = "path")})
    @GetMapping(value = "/my/pay‐channel‐params/apps/{appId}/platformchannels/{platformChannel}/pay‐channels/{payChannel}")
    public PayChannelParamDTO queryPayChannelParam(@PathVariable String appId, @PathVariable String platformChannel, @PathVariable String payChannel) {
        PayChannelParamDTO payChannelParamDTO = payChannelService.queryParamByAppPlatformAndPayChannel(appId, platformChannel, payChannel);
        return payChannelParamDTO;
    }

}
