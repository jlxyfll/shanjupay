package com.shanjupay.merchant.controller;

import com.shanjupay.merchant.api.MerchantService;
import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.convert.MerchantConvert;
import com.shanjupay.merchant.service.SmsService;
import com.shanjupay.merchant.vo.MerchantRegisterVO;
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
    @ApiImplicitParam(name = "phone", value = "手机号", required = true, dataType = "String", paramType = "query")
    @GetMapping(value = "/sms")
    public String getSMSCode(@RequestParam("phone") String phone) {
        log.info("向手机号:{}发送验证码", phone);
        String s = smsService.sendMsg(phone);
        return s;
    }

    @ApiOperation("商户注册")
    @ApiImplicitParam(name = "merchantRegisterVO", value = "注册信息", required = true, dataType = "MerchantRegisterVO", paramType = "body")
    @PostMapping(value = "/merchants/register")
    public MerchantRegisterVO merchantRegister(@RequestBody MerchantRegisterVO merchantRegisterVO) {
/*        // 校验验证码
        String verifiyCode = merchantRegisterVO.getVerifiyCode();
        String verifiykey = merchantRegisterVO.getVerifiykey();
        smsService.checkVerifiyCode(verifiykey, verifiyCode);
        // 调用dubbo服务接口
        MerchantDTO merchantDTO = new MerchantDTO();
        merchantDTO.setMobile(merchantRegisterVO.getMobile());
        merchantDTO.setUsername(merchantRegisterVO.getUsername());
        // 向dto写入商户注册的信息
        merchantService.createMerchant(merchantDTO);*/
        // 校验验证码
        String verifiyCode = merchantRegisterVO.getVerifiyCode();
        String verifiykey = merchantRegisterVO.getVerifiykey();
        smsService.checkVerifiyCode(verifiykey, verifiyCode);
        // 调用dubbo服务接口
        MerchantDTO merchantDTO = MerchantConvert.INSTANCE.vo2DTO(merchantRegisterVO);
        merchantService.createMerchant(merchantDTO);
        return merchantRegisterVO;
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
