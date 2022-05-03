package com.atguigu.yygh.config;

import com.google.common.base.Predicates;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author Xu
 * @date 2022/4/12 18:39
 * yygh_parent com.atguigu.yygh.common.config
 */
@Configuration
@EnableSwagger2
public class Swagger2Config {
    @Bean
    public Docket adminApiConfig() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("adminApi")
                .apiInfo(adminApiInfo())
                .select()
                .paths(Predicates.and(PathSelectors.regex("/admin/.*")))
                .build();
    }

    @Bean
    public Docket userApiConfig() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("userApi")
                .apiInfo(userApiInfo())
                .select()
                .paths(Predicates.and(PathSelectors.regex("/user/.*")))
                .build();
    }
    @Bean
    public Docket apiApiConfig() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("api")
                .apiInfo(userApiInfo())
                .select()
                .paths(Predicates.and(PathSelectors.regex("/api/.*")))
                .build();
    }

    private ApiInfo userApiInfo() {
        return new ApiInfoBuilder()
                .title("用户系统Api文档")
                .description("用户系统各个接口文档")
                .version("v1.0")
                .contact(new Contact("小徐","http://www.baidu.com","1273343014@qq.com"))
                .build();
    }

    private ApiInfo adminApiInfo() {
        return new ApiInfoBuilder()
                .title("管理员系统Api文档")
                .description("管理员系统各个接口文档")
                .version("v1.0")
                .contact(new Contact("小徐","http://www.baidu.com","1273343014@qq.com"))
                .build();
    }
}
