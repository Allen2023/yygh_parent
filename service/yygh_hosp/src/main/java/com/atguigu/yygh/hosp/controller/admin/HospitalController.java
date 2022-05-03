package com.atguigu.yygh.hosp.controller.admin;

import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.model.hosp.Hospital;
import com.atguigu.yygh.result.R;
import com.atguigu.vo.hosp.HospitalQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

/**
 * @author Xu
 * @date 2022/4/19 20:36
 * yygh_parent com.atguigu.yygh.hosp.controller.admin
 */
@Api(description = "医院接口")
@RestController
@RequestMapping("/admin/hosp/hospital")
public class HospitalController {

    @Autowired
    private HospitalService hospitalService;

    @ApiOperation(value = "获取分页列表")
    @GetMapping("/{page}/{limit}")
    public R getHospitalPage(@PathVariable Integer page, @PathVariable Integer limit, HospitalQueryVo hospitalQueryVo) {
        Page<Hospital> page1 = hospitalService.getHospitalPage(page, limit, hospitalQueryVo);
        return R.ok().data("total", page1.getTotalElements()).data("rows", page1.getContent());
    }

    @ApiOperation(value = "更新上线状态")
    @PutMapping("/updateStatus/{id}/{status}")
    public R lock(@PathVariable String id, @PathVariable Integer status) {
        hospitalService.lock(id, status);
        return R.ok();
    }

    @ApiOperation(value = "获取医院详情")
    @GetMapping("/show/{id}")
    public R show(@PathVariable String id) {
        return R.ok().data("hospital", hospitalService.show(id));
    }
}
