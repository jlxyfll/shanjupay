package com.shanjupay.merchant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @title: MerchantBootstrap
 * @Author Tang Xiaojiang
 * @Date: 2020/11/20 12:04
 * @Version 1.0
 */
@SpringBootApplication
@EnableDiscoveryClient
public class MerchantBootstrap {
    public static void main(String[] args) {
        SpringApplication.run(MerchantBootstrap.class, args);
    }
}
