package com.shanjupay.merchant.service.impl;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.QiniuUtils;
import com.shanjupay.merchant.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.BatchUpdateException;

/**
 * @title: FileServiceImpl
 * @Author Tang Xiaojiang
 * @Date: 2020/11/21 23:52
 * @Version 1.0
 */
@Service
@Slf4j
public class FileServiceImpl implements FileService {
    @Value("${oss.qiniu.url}")
    private String qiniuUrl;
    @Value("${oss.qiniu.accessKey}")
    private String accessKey;
    @Value("${oss.qiniu.secretKey}")
    private String secretKey;
    @Value("${oss.qiniu.bucket}")
    private String bucket;

    /**
     * @param bytes
     * @param fileName
     * @return
     * @throws BatchUpdateException
     */
    @Override
    public String upload(byte[] bytes, String fileName) throws BatchUpdateException {
        // String accessKey, String secretKey, String bucket, String fileName, byte[] bytes
        try {
            QiniuUtils.upload2QiNiu(accessKey, secretKey, bucket, fileName, bytes);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(CommonErrorCode.E_100106);
        }
        // 返回文件名称
        return qiniuUrl + fileName;
    }
}
