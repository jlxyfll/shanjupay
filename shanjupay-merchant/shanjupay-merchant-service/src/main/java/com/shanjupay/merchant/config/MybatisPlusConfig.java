package com.shanjupay.merchant.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.plugins.PerformanceInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RestController;

/**
 * @title: MybatisPlusConfig
 * @Author Tang Xiaojiang
 * @Date: 2020/11/20 13:49
 * @Version 1.0
 */
@RestController
@MapperScan(value = "com.shanjupay.**.mapper")
public class MybatisPlusConfig {
    /**
     * 分页插件，自动识别数据库类型
     */
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        return new PaginationInterceptor();
    }

    /**
     * 启用性能分析插件
     */
    @Bean
    public PerformanceInterceptor performanceInterceptor() {
        return new PerformanceInterceptor();
    }
}
