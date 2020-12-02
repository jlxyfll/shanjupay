package com.shanjupay.merchant.controller;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.PhoneUtil;
import com.shanjupay.common.util.QRCodeUtil;
import com.shanjupay.merchant.api.ApplyMerchantService;
import com.shanjupay.merchant.convert.MerchantDetailConvert;
import com.shanjupay.merchant.service.FileService;
import com.shanjupay.merchant.api.MerchantService;
import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.convert.MerchantConvert;
import com.shanjupay.merchant.service.SmsService;
import com.shanjupay.merchant.common.util.SecurityUtil;
import com.shanjupay.merchant.vo.MerchantDetailVO;
import com.shanjupay.merchant.vo.MerchantRegisterVO;
import com.shanjupay.transaction.api.TransactionService;
import com.shanjupay.transaction.api.dto.QrCodeDTO;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    /**
     * 门店二维码订单标题
     */
    @Value("${shanjupay.c2b.subject}")
    private String subject;

    /**
     * 门店二维码订单内容
     */
    @Value("${shanjupay.c2b.body}")
    private String body;

    @Reference
    private MerchantService merchantService;

    @Reference
    private ApplyMerchantService applyMerchantService;

    @Autowired
    private FileService fileService;

    @Autowired
    private SmsService smsService;

    @Reference
    private TransactionService transactionService;

    @ApiOperation(value = "根据id查询商户")
    @GetMapping(value = "/merchants/{id}")
    public MerchantDTO queryMerchantById(@PathVariable("id") Long id) {
        MerchantDTO merchantDTO = merchantService.queryMerchantById(id);
        return merchantDTO;
    }

    @ApiOperation("获取登录用户的商户信息")
    @GetMapping(value = "/my/merchants")
    public MerchantDTO getMyMerchantInfo() {
        Long merchantId = SecurityUtil.getMerchantId();
        MerchantDTO merchantDTO = merchantService.queryMerchantById(merchantId);
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

    @ApiOperation("商户资质申请")
    @ApiImplicitParam(name = "merchantDetailVO", value = "商户认证资料", required = true, dataType = "MerchantDetailVO", paramType = "body")
    @PostMapping(value = "/my/merchants/save")
    public void saveMerchant(@RequestBody MerchantDetailVO merchantDetailVO) {
        // 解析token得到商户id
        Long merchantId = SecurityUtil.getMerchantId();
        MerchantDTO merchantDTO = MerchantDetailConvert.INSTANCE.vo2DTO(merchantDetailVO);
        // 资质申请

        applyMerchantService.applyMerchant(merchantId, merchantDTO);
    }

    @ApiOperation("生成商户应用门店二维码")
    @ApiImplicitParams({@ApiImplicitParam(name = "appId", value = "商户应用id", required = true, dataType = "String", paramType = "path")
            , @ApiImplicitParam(name = "storeId", value = "商户门店id", required = true, dataType = "String", paramType = "path")})
    @GetMapping(value = "/my/apps/{appId}/stores/{storeId}/app-store-qrcode")
    public String createCScanBStoreQRCode(@PathVariable String appId, @PathVariable Long storeId) throws BusinessException {
        // 商户id
        Long merchantId = SecurityUtil.getMerchantId();
        // 生成二维码链接
        QrCodeDTO qrCodeDTO = new QrCodeDTO();
        qrCodeDTO.setMerchantId(merchantId);
        qrCodeDTO.setAppId(appId);
        qrCodeDTO.setStoreId(storeId);
        // 标题
        MerchantDTO merchantDTO = merchantService.queryMerchantById(merchantId);
        // "%s 商品"
        qrCodeDTO.setSubject(String.format(subject, merchantDTO.getMerchantName()));
        // 内容，格式："向%s 付款"
        qrCodeDTO.setBody(String.format(body, merchantDTO.getMerchantName()));

        String storeQrCodeUrl = transactionService.createStoreQrCode(qrCodeDTO);
        log.info("[merchantId:{},appId:{},storeId:{}]createCScanBStoreQRCode is:{}", merchantId, appId, storeId, storeQrCodeUrl);
        try {
            // 根据返回url，调用生成二维码工具类，生成二维码base64返回
            QRCodeUtil qrCodeUtil = new QRCodeUtil();
            return qrCodeUtil.createQRCode(storeQrCodeUrl, 200, 200);
        } catch (IOException e) {
            throw new BusinessException(CommonErrorCode.E_200007);
        }

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
