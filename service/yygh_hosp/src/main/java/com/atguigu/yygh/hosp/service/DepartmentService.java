package com.atguigu.yygh.hosp.service;

import com.atguigu.model.hosp.Department;
import com.atguigu.vo.hosp.DepartmentVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * @author Xu
 * @date 2022/4/19 18:35
 * yygh_parent com.atguigu.yygh.hosp.service
 */

public interface DepartmentService {
    void save(Map<String, Object> stringObjectMap);

    Page<Department> getDepartmentList(Map<String, Object> stringObjectMap);

    void removeDepartment(Map<String, Object> stringObjectMap);

    List<DepartmentVo> getDeptList(String hoscode);



    Department findByHoscodeAndDepcode(String hoscode, String depcode);
}
