package com.atguigu.yygh.user.client;

import com.atguigu.model.user.Patient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author Xu
 * @date 2022/4/27 22:55
 * yygh_parent com.atguigu.yygh.user.client
 */
@FeignClient("service-user")
public interface PatientFeignClient {
    //远程调用PathVariable一定要指定value属性值
    @GetMapping("/api/userinfo/patient/auth/remote/{id}")
    public Patient getPatientInfo(@PathVariable("id") Long id);
}
