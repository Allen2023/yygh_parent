package com.atguigu.yygh.hosp.controller.user;

import com.atguigu.model.hosp.Hospital;
import com.atguigu.vo.hosp.DepartmentVo;
import com.atguigu.vo.hosp.HospitalQueryVo;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.result.R;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author Xu
 * @date 2022/4/22 20:12
 * yygh_parent com.atguigu.yygh.hosp.controller.user
 */
@RestController
@RequestMapping("/user/hosp/hospital")
public class UserHospitalController {

    @Autowired
    private HospitalService hospitalService;
    @Autowired
    private DepartmentService departmentService;

    @ApiOperation(value = "获取用户端医院分页列表")
    @GetMapping("/list")
    public R index(HospitalQueryVo hospitalQueryVo) {
        Page<Hospital> hospitalPage = hospitalService.getHospitalPage(1, 1000, hospitalQueryVo);
        return R.ok().data("pages", hospitalPage.getContent());
    }

    @ApiOperation(value = "模糊搜索医院")
    @GetMapping("/findByHosname/{hosname}")
    public R getHospitalName(@PathVariable String hosname) {
        List<Hospital> hospitalList = hospitalService.findByHosname(hosname);
        return R.ok().data("list", hospitalList);
    }

    @ApiOperation(value = "根据医院编号获取医院详情")
    @GetMapping("/{hoscode}")
    public R show(@PathVariable String hoscode) {
        Map<String, Object> map = hospitalService.findByHoscode(hoscode);
        return R.ok().data(map);
    }


    @ApiOperation(value = "获取科室列表")
    @GetMapping("/department/{hoscode}")
    public R getDepartmentList(@PathVariable String hoscode) {
        List<DepartmentVo> list = departmentService.getDeptList(hoscode);
        return R.ok().data("list", list);
    }
}
