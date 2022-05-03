package com.atguigu.yygh.hosp.service;

import com.atguigu.model.hosp.Hospital;
import com.atguigu.vo.hosp.HospitalQueryVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * @author Xu
 * @date 2022/4/19 1:30
 * yygh_parent com.atguigu.yygh.hosp.service
 */
public interface HospitalService {

    void save(Map<String, Object> resultMap);

    Hospital getByHoscode(String hoscode);

    Page<Hospital> getHospitalPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo);

    void lock(String id, Integer status);

    Map<String, Object> show(String id);

    List<Hospital> findByHosname(String hosname);


    Map<String, Object> findByHoscode(String hoscode);
}
