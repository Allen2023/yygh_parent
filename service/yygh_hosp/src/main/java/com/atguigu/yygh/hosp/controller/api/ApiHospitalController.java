package com.atguigu.yygh.hosp.controller.api;

import com.atguigu.yygh.exception.YYGHException;
import com.atguigu.yygh.hosp.bean.Result;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.hosp.utils.HttpRequestHelper;
import com.atguigu.yygh.hosp.utils.MD5;
import com.atguigu.model.hosp.Hospital;
import com.atguigu.model.hosp.HospitalSet;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author Xu
 * @date 2022/4/19 1:00
 * yygh_parent com.atguigu.yygh.hosp.controller.api
 */
@RestController
@RequestMapping("/api/hosp")
public class ApiHospitalController {
    @Autowired
    private HospitalService hospitalService;
    @Autowired
    private HospitalSetService hospitalSetService;

    @ApiOperation(value = "上传医院信息")
    @PostMapping("/saveHospital")
    public Result saveHospital(HttpServletRequest request) {
        Map<String, Object> resultMap = HttpRequestHelper.switchMap(request.getParameterMap());
        String sign = (String) resultMap.get("sign");
        Object hoscode = resultMap.get("hoscode");
        QueryWrapper<HospitalSet> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("hoscode", hoscode);
        //根据hoscode查询得到对象
        HospitalSet hospitalSet = hospitalSetService.getOne(queryWrapper);
        String signKey = hospitalSet.getSignKey();
        String encrypt = MD5.encrypt(signKey);
        if (!StringUtils.isEmpty(encrypt) && !StringUtils.isEmpty(sign) && sign.equals(encrypt)) {
            String logoData = (String) resultMap.get("logoData");
            logoData = logoData.replaceAll(" ", "+");
            resultMap.put("logoData", logoData);
            hospitalService.save(resultMap);
        }
        hospitalService.save(resultMap);
        return Result.ok();
    }

    @ApiOperation(value = "查询医院信息")
    @PostMapping("/hospital/show")
    public Result getHospital(HttpServletRequest request) {
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        String hoscode = (String) paramMap.get("hoscode");
        if (StringUtils.isEmpty(hoscode)) {
            throw new YYGHException(20001, "失败");
        }
        Hospital hospital = hospitalService.getByHoscode(hoscode);
        return Result.ok(hospital);
    }




}
