package com.atguigu.yygh.user.controller.user;


import com.atguigu.model.user.Patient;
import com.atguigu.yygh.result.R;
import com.atguigu.yygh.user.service.PatientService;
import com.atguigu.yygh.utils.JwtHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 * 就诊人表 前端控制器
 * </p>
 *
 * @author atguigu
 * @since 2022-04-26
 */
@RestController
@RequestMapping("/api/userinfo/patient/")
public class PatientController {
    @Autowired
    private PatientService patientService;

    //获取就诊人列表
    @GetMapping("auth/findAll")
    public R findAll(HttpServletRequest request) {
        //获取当前登录用户id
        String token = request.getHeader("token");
        Long userId = JwtHelper.getUserId(token);
        List<Patient> list = patientService.findAllUserId(userId);
        return R.ok().data("list", list);
    }

    //添加就诊人
    @PostMapping("auth/save")
    public R savePatient(@RequestBody Patient patient, HttpServletRequest request) {
        //获取当前登录用户id
        String token = request.getHeader("token");
        Long userId = JwtHelper.getUserId(token);
        patient.setUserId(userId);
        patientService.save(patient);
        return R.ok();
    }

    //根据id获取就诊人信息，修改就诊人信息时回显
    @GetMapping("auth/get/{id}")
    public R getPatient(@PathVariable Long id) {
        Patient patient = patientService.getPatientId(id);
        return R.ok().data("patient", patient);
    }

    //根据id获取就诊人信息，修改就诊人信息时回显  "远程调用"
    @GetMapping("auth/remote/{id}")
    public Patient getPatientInfo(@PathVariable("id") Long id) {
        Patient patient = patientService.getPatientId(id);
        return patient;
    }

    //修改就诊人
    @PostMapping("auth/update")
    public R updatePatient(@RequestBody Patient patient) {
        patientService.updateById(patient);
        return R.ok();
    }

    //删除就诊人
    @DeleteMapping("auth/remove/{id}")
    public R removePatient(@PathVariable Long id) {
        patientService.removeById(id);
        return R.ok();
    }

}

