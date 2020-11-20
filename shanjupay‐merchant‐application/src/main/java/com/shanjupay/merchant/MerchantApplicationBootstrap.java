package com.shanjupay.merchant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @title: MerchantApplicationBootstrap
 * @Author Tang Xiaojiang
 * @Date: 2020/11/20 0:34
 * @Version 1.0
 */
@SpringBootApplication
@EnableDiscoveryClient
public class MerchantApplicationBootstrap {
    public static void main(String[] args) {
        SpringApplication.run(MerchantApplicationBootstrap.class, args);
    }
}
