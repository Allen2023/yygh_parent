package com.atguigu.yygh.user.prop;

import lombok.Data;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author Xu
 * @date 2022/4/23 19:46
 * yygh_parent com.atguigu.yygh.user.prop
 */
@Data
@ConfigurationProperties(prefix = "wx")
public class WXProperties {

    private String appId;
    private String appSecret;
    private String redirectUrl;
}
