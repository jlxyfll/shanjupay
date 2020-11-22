package com.shanjupay.merchant.service;

import java.sql.BatchUpdateException;

/**
 * @title: FileService
 * @Author Tang Xiaojiang
 * @Date: 2020/11/21 23:51
 * @Version 1.0
 */
public interface FileService {
    /**
     * 文件服务
     * @param bytes
     * @param fileName
     * @return
     * @throws BatchUpdateException
     */
    public String upload(byte[] bytes, String fileName) throws BatchUpdateException;
}
