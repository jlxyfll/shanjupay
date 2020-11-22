package com.shanjupay.merchant.controller;

import com.shanjupay.merchant.api.AppService;
import com.shanjupay.merchant.api.dto.AppDTO;
import com.shanjupay.merchant.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.List;

/**
 * @title: AppController
 * @Author Tang Xiaojiang
 * @Date: 2020/11/22 13:16
 * @Version 1.0
 */
@Api(value = "商户平台‐应用管理", tags = "商户平台‐应用相关", description = "商户平台‐应用相关")
@RestController
public class AppController {

    @Reference
    private AppService appService;

    @ApiOperation("商户创建应用")
    @ApiImplicitParam(name = "appDTO", value = "应用信息", required = true, dataType = "AppDTO", paramType = "body")
    @PostMapping(value = "/my/apps")
    public AppDTO createApp(@RequestBody AppDTO appDTO) {
        Long merchantId = SecurityUtil.getMerchantId();
        return appService.createApp(merchantId, appDTO);
    }

    @ApiOperation("查询商户下的应用列表")
    @GetMapping(value = "/my/apps")
    public List<AppDTO> queryMyApps() {
        Long merchantId = SecurityUtil.getMerchantId();
        List<AppDTO> appDTOList = appService.queryAppByMerchant(merchantId);
        return appDTOList;
    }

    @ApiOperation("根据appid获取应用的详细信息")
    @ApiImplicitParam(name = "appId", value = "商户应用ID", required = true, dataType = "String", paramType = "path")
    @GetMapping(value = "/my/apps/{appId}")
    public AppDTO getApp(@PathVariable("appId") String appId) {
        AppDTO appDTO = appService.getAppById(appId);
        return appDTO;
    }

}
