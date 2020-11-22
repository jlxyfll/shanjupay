package com.shanjupay.merchant.controller;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.PhoneUtil;
import com.shanjupay.merchant.service.FileService;
import com.shanjupay.merchant.api.MerchantService;
import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.convert.MerchantConvert;
import com.shanjupay.merchant.service.SmsService;
import com.shanjupay.merchant.vo.MerchantRegisterVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.BatchUpdateException;
import java.util.UUID;

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
    private FileService fileService;

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
    public MerchantRegisterVO merchantRegister(@RequestBody MerchantRegisterVO merchantRegisterVO) throws BusinessException {
        // 1.校验参数
        if (merchantRegisterVO == null) {
            // 参数为空
            throw new BusinessException(CommonErrorCode.E_100108);
        }
        // 2.校验手机号
        if (StringUtils.isBlank(merchantRegisterVO.getMobile())) {
            // 手机号为空
            throw new BusinessException(CommonErrorCode.E_100112);
        }
        // 3.校验手机号合法性
        if (!PhoneUtil.isMatches(merchantRegisterVO.getMobile())) {
            // 手机号不合法
            throw new BusinessException(CommonErrorCode.E_100109);
        }
        // 4.校验联系人
        if (StringUtils.isBlank(merchantRegisterVO.getUsername())) {
            // 联系人为空
            throw new BusinessException(CommonErrorCode.E_100110);
        }
        // 5.密码非空校验
        if (StringUtils.isBlank(merchantRegisterVO.getPassword())) {
            throw new BusinessException(CommonErrorCode.E_100111);
        }
        // 6.验证码非空校验
        if (StringUtils.isBlank(merchantRegisterVO.getVerifiyCode())) {
            throw new BusinessException(CommonErrorCode.E_100103);
        }
        // 7.校验smskey
        // if (StringUtils.isBlank(merchantRegisterVO.getVerifiykey())){

        // }
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
        /*int i = 1 / 0;*/
        // 校验验证码
        String verifiyCode = merchantRegisterVO.getVerifiyCode();
        String verifiykey = merchantRegisterVO.getVerifiykey();
        smsService.checkVerifiyCode(verifiykey, verifiyCode);
        // 调用dubbo服务接口
        MerchantDTO merchantDTO = MerchantConvert.INSTANCE.vo2DTO(merchantRegisterVO);
        merchantService.createMerchant(merchantDTO);
        return merchantRegisterVO;
    }

    @ApiOperation("证件上传")
    @ApiParam(value = "上传的文件", required = true)
    @PostMapping(value = "/upload")
    public String upload(@RequestParam("file") MultipartFile file) throws Exception {
        // 原始文件名称
        String originalFilename = file.getOriginalFilename();
        // 文件后缀
        int i = originalFilename.lastIndexOf(".");
        String substring = originalFilename.substring(i);
        // 文件名称
        String fileName = UUID.randomUUID() + substring;

        String fileUrl = fileService.upload(file.getBytes(), fileName);
        return fileUrl;
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
