package com.shanjupay.merchant.service;

import com.shanjupay.merchant.vo.MerchantRegisterVO;

/**
 * @title: SmsService
 * @Author Tang Xiaojiang
 * @Date: 2020/11/20 22:48
 * @Version 1.0
 */
public interface SmsService {
    /**
     * 发送验证码
     * @param phone
     * @return
     */
    String sendMsg(String phone);

    /**
     * 校验手机验证码
     * @param verifiyKey 验证码key
     * @param verifiyCode 验证码
     */
    void  checkVerifiyCode(String verifiyKey, String verifiyCode);
}
