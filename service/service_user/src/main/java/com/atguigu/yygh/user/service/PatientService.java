package com.atguigu.yygh.user.service;


import com.atguigu.model.user.Patient;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 就诊人表 服务类
 * </p>
 *
 * @author atguigu
 * @since 2022-04-26
 */
public interface PatientService extends IService<Patient> {

    List<Patient> findAllUserId(Long userId);

    Patient getPatientId(Long id);
}
