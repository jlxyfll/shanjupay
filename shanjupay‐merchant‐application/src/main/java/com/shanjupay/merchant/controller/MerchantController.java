package com.shanjupay.merchant.controller;

import com.shanjupay.merchant.api.MerchantService;
import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.service.SmsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.aspectj.apache.bcel.generic.RET;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @title: MerchantController
 * @Author Tang Xiaojiang
 * @Date: 2020/11/20 15:12
 * @Version 1.0
 */
@RestController
@Api(value = "商户平台-商户相关", tags = "商户平台-商户相关", description = "商户平台-商户相关")
@Slf4j
public class MerchantController {
    @Reference
    private MerchantService merchantService;

    @Autowired
    private SmsService smsService;

    @ApiOperation(value = "根据id查询商户")
    @GetMapping(value = "/merchants/{id}")
    public MerchantDTO queryMerchantById(@PathVariable("id") Long id) {
        MerchantDTO merchantDTO = merchantService.queryMerchantById(id);
        return merchantDTO;
    }

    @ApiOperation("获取手机验证码")
    @ApiImplicitParam(name = "phone",value = "手机号",required = true,dataType = "String",paramType = "query")
    @GetMapping(value = "/sms")
    public String getSMSCode(@RequestParam("phone") String phone){
        log.info("向手机号:{}发送验证码",phone);
        String s = smsService.sendMsg(phone);
        return s;
    }
    @ApiOperation("测试")
    @GetMapping(path = "/hello")
    public String hello() {
        return "hello";
    }

    @ApiOperation("测试")
    @ApiImplicitParam(name = "name", value = "姓名", required = true, dataType = "string")
    @PostMapping(value = "/hi")
    public String hi(String name) {
        return "hi," + name;
    }
}
