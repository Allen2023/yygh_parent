package com.atguigu.yygh.hosp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author Xu
 * @date 2022/4/12 11:32
 * yygh_parent com.atguigu.yygh.hosp
 */

@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.atguigu")
@ComponentScan("com.atguigu")
@MapperScan("com.atguigu.yygh.hosp.mapper")
@SpringBootApplication
public class ServiceHospApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceHospApplication.class, args);
    }
}
