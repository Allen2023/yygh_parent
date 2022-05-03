package com.atguigu.yygh.hosp.controller.api;

import com.atguigu.yygh.hosp.bean.Result;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.utils.HttpRequestHelper;
import com.atguigu.model.hosp.Department;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author Xu
 * @date 2022/4/19 18:18
 * yygh_parent com.atguigu.yygh.hosp.controller.api
 */
@RestController
@RequestMapping("/api/hosp")
public class ApiDepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @ApiOperation(value = "上传科室信息")
    @PostMapping("/saveDepartment")
    public Result saveHospital(HttpServletRequest request) {
        Map<String, Object> stringObjectMap = HttpRequestHelper.switchMap(request.getParameterMap());
        departmentService.save(stringObjectMap);
        return Result.ok();
    }

    @ApiOperation(value = "查询科室信息")
    @PostMapping("/department/list")
    public Result getDepartmentList(HttpServletRequest request) {
        Map<String, Object> stringObjectMap = HttpRequestHelper.switchMap(request.getParameterMap());
        Page<Department> departmentPage = departmentService.getDepartmentList(stringObjectMap);
        return Result.ok(departmentPage);
    }

    @ApiOperation(value = "删除科室信息")
    @PostMapping("/department/remove")
    public Result removeDepartment(HttpServletRequest request) {
        Map<String, Object> stringObjectMap = HttpRequestHelper.switchMap(request.getParameterMap());
        departmentService.removeDepartment(stringObjectMap);
        return Result.ok();
    }
}
