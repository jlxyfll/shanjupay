package com.shanjupay.common.util;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import com.qiniu.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.UUID;

/**
 * @title: QiniuUtils
 * @Author Tang Xiaojiang
 * @Date: 2020/11/21 21:19
 * @Version 1.0
 */
public class QiniuUtils {

    /**
     * 记录日志
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(QiniuUtils.class);

    /**
     * 提供上传文件工具方法，accessKey,secretKey,bucket,fileName
     * @param accessKey
     * @param secretKey
     * @param bucket
     * @param fileName
     * @param bytes
     */
    public static void upload2QiNiu(String accessKey, String secretKey, String bucket, String fileName, byte[] bytes){
        //构造一个带指定 Region 对象的配置类
        Configuration cfg = new Configuration(Region.huabei());
        //...其他参数参考类注释
        UploadManager uploadManager = new UploadManager(cfg);
        //默认不指定key的情况下，以文件内容的hash值作为文件名
        String key = fileName;
        try {
            // 认证
            Auth auth = Auth.create(accessKey, secretKey);
            // 认证通过，得到token
            String upToken = auth.uploadToken(bucket);
            try {
                // 上传文件 参数：字节数组，key，token令牌
                Response response = uploadManager.put(bytes, key, upToken);
                //解析上传成功的结果
                DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
                System.out.println(putRet.key);
                System.out.println(putRet.hash);
            } catch (QiniuException ex) {
                Response r = ex.response;
                System.err.println(r.toString());
                LOGGER.error("上传文件到七牛云:{}",ex.getMessage());
                try {
                    System.err.println(r.bodyString());
                    LOGGER.error(r.bodyString());
                } catch (QiniuException ex2) {
                    //ignore
                }
                throw new RuntimeException(r.bodyString());
            }
        } catch (IOException ex) {
            LOGGER.error("上传文件到七牛云:{}",ex.getMessage());
            throw new RuntimeException(ex.getMessage());
        }
    }

    private static void testUp() {
        //构造一个带指定 Region 对象的配置类
        Configuration cfg = new Configuration(Region.huabei());
        //...其他参数参考类注释
        UploadManager uploadManager = new UploadManager(cfg);
        //...生成上传凭证，然后准备上传
        String accessKey = "d2JMM_9hFQEKKCGl2Hb8-Ncu34n6lfnKLP2K-JHC";
        String secretKey = "BOlJ7_-pYQFcMkAIn3MP88fQ3VaIrUPg1q9a8R5v";
        String bucket = "shanjupay-jiang";
        //默认不指定key的情况下，以文件内容的hash值作为文件名
        String key = null;
        FileInputStream fileInputStream = null;
        try {
            // 得到本地文件的字节数组
            String filePath = "E:\\系统\\图片\\伤脑筋.jpg";
            fileInputStream = new FileInputStream(new File(filePath));
            byte[] uploadBytes = IOUtils.toByteArray(fileInputStream);
            key = UUID.randomUUID() + ".jpg";
            // byte[] uploadBytes = "hello qiniu cloud".getBytes("utf-8");
            // 认证
            Auth auth = Auth.create(accessKey, secretKey);
            // 认证通过，得到token
            String upToken = auth.uploadToken(bucket);
            try {
                // 上传文件
                Response response = uploadManager.put(uploadBytes, key, upToken);
                //解析上传成功的结果
                DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
                System.out.println(putRet.key);
                System.out.println(putRet.hash);
            } catch (QiniuException ex) {
                Response r = ex.response;
                System.err.println(r.toString());
                try {
                    System.err.println(r.bodyString());
                } catch (QiniuException ex2) {
                    //ignore
                }
            }
        } catch (IOException ex) {

        }
    }

    private static void testDown() throws UnsupportedEncodingException {
        /*String fileName = "63173605-78fe-44e0-9c92-2ef587921f1b.jpg";
        String domainOfBucket = "http://qk5djy7cp.hb-bkt.clouddn.com/";
        String finalUrl = String.format("%s/%s", domainOfBucket, fileName);
        System.out.println(finalUrl);*/
        String fileName = "63173605-78fe-44e0-9c92-2ef587921f1b.jpg";
        String domainOfBucket = "http://qk5djy7cp.hb-bkt.clouddn.com/";
        String encodedFileName = URLEncoder.encode(fileName, "utf-8").replace("+", "%20");
        String publicUrl = String.format("%s/%s", domainOfBucket, encodedFileName);
        String accessKey = "d2JMM_9hFQEKKCGl2Hb8-Ncu34n6lfnKLP2K-JHC";
        String secretKey = "BOlJ7_-pYQFcMkAIn3MP88fQ3VaIrUPg1q9a8R5v";
        Auth auth = Auth.create(accessKey, secretKey);
        long expireInSeconds = 3600;//1小时，可以自定义链接过期时间
        String finalUrl = auth.privateDownloadUrl(publicUrl, expireInSeconds);
        System.out.println(finalUrl);
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
//        testUp();

        testDown();
    }
}
