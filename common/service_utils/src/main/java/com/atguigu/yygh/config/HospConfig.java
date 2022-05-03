package com.atguigu.yygh.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Xu
 * @date 2022/4/12 16:15
 * yygh_parent com.atguigu.yygh.hosp.config
 */
@Configuration
public class HospConfig {

    /**
     * 分页插件
     */
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        return new PaginationInterceptor();
    }
}
