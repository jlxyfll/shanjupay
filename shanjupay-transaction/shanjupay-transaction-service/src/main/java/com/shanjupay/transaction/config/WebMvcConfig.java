package com.shanjupay.transaction.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @title: WebMvcConfig
 * @Author Tang Xiaojiang
 * @Date: 2020/11/28 10:23
 * @Version 1.0
 */
@Component
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/pay-page").setViewName("pay");
        registry.addViewController("/pay-page-error").setViewName("pay_error");

    }
}
