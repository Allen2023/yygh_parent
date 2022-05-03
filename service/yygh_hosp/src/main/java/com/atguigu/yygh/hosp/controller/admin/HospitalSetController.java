package com.atguigu.yygh.hosp.controller.admin;


import com.atguigu.yygh.result.R;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.hosp.utils.MD5;
import com.atguigu.model.hosp.HospitalSet;
import com.atguigu.vo.hosp.HospitalSetQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;

/**
 * <p>
 * 医院设置表 前端控制器
 * </p>
 *
 * @author atguigu
 * @since 2022-04-12
 */

@Api(tags = "管理员系统-医院设置信息")
@RestController
@RequestMapping("/admin/hosp/hospitalSet")
@Slf4j
public class HospitalSetController {

    @Autowired
    private HospitalSetService hospitalSetService;

    /**
     * 定义在Controller上@Api
     * 定义在方法上：@ApiOperation
     * 定义在参数上：@ApiParam
     * 定义在pojo上：@ApiModel
     * 定义在pojo的属性上：@ApiModelProperty
     * @return
     */

    @ApiOperation(value = "查询所有医院设置")
    @GetMapping("/findALL")
    public R findALL() {
        /*try {
            int result = 1 / 0;
        } catch (Exception e) {
          throw new YYGHException(20001, "查询异常");
        }*/
        //log.info("thread"+Thread.currentThread().getId());
        List<HospitalSet> list = hospitalSetService.list();
        return R.ok().data("list", list);
    }


    @ApiOperation(value = "根据id医院设置删除")
    @DeleteMapping("/delete/{id}")
    public R delete(@ApiParam(name = "id", value = "医院设置id", required = true) @PathVariable(value = "id") Long id) {
        boolean flag = hospitalSetService.removeById(id);
        if (flag) {
            return R.ok();
        } else {
            return R.error();
        }
    }

    @ApiOperation(value = "分页医院设置列表")
    @PostMapping("/page/{current}/{size}")
    public R pageList(
            @ApiParam(name = "current", value = "当前页码", required = true)
            @PathVariable Long current,
            @ApiParam(name = "size", value = "每页记录数", required = true)
            @PathVariable Long size,
            @ApiParam(name = "hospitalSetQueryVo", value = "查询对象", required = false)
            @RequestBody HospitalSetQueryVo hospitalSetQueryVo) {
        /**
         *   简单查询
         */
       /* Page<HospitalSet> pageParam = new Page<>(page, limit);
          hospitalSetService.page(pageParam);
        long total = pageParam.getTotal();
        List<HospitalSet> records = pageParam.getRecords();
        return R.ok().data("total",total).data("rows", records);*/

        /**
         * 条件查询
         */
        Page<HospitalSet> pageParam = new Page<>(current, size);
        QueryWrapper<HospitalSet> queryWrapper = new QueryWrapper<>();
        if (hospitalSetQueryVo == null) {
            hospitalSetService.page(pageParam, queryWrapper);
        } else {
            String hosname = hospitalSetQueryVo.getHosname();
            String hoscode = hospitalSetQueryVo.getHoscode();
            //对医院名称和医院编号做非空校验
            if (!StringUtils.isEmpty(hosname)) {
                queryWrapper.like("hosname", hosname);
            }
            if (!StringUtils.isEmpty(hoscode)) {
                queryWrapper.eq("hoscode", hoscode);
            }
            hospitalSetService.page(pageParam, queryWrapper);
        }
        long total = pageParam.getTotal();
        List<HospitalSet> records = pageParam.getRecords();
        return R.ok().data("total", total).data("rows", records);
    }

    @ApiOperation(value = "新增医院设置")
    @PostMapping("/saveHospSet")
    public R save(
            @ApiParam(name = "hospitalSet", value = "医院设置对象", required = true)
            @RequestBody HospitalSet hospitalSet) {
        //设置状态1使用,0不能使用
        hospitalSet.setStatus(1);
        //签名秘钥
        Random random = new Random();
        hospitalSet.setSignKey(MD5.encrypt(System.currentTimeMillis() + "" + random.nextInt(1000)));
        hospitalSetService.save(hospitalSet);
        return R.ok();
    }


    @ApiOperation(value = "根据id查询医院设置")
    @GetMapping("/detail/{id}")
    public R getHospSet(
            @ApiParam(name = "id", value = "医院设置id", required = true)
            @PathVariable Long id) {
        return R.ok().data("hospitalSet", hospitalSetService.getById(id));
    }

    @ApiOperation(value = "根据id修改医院设置")
    @PutMapping("/updateHospSet")
    public R updateHospSet(
            @ApiParam(name = "hospitalSet", value = "医院设置对象", required = true)
            @RequestBody HospitalSet hospitalSet) {
        boolean flag = hospitalSetService.updateById(hospitalSet);
        if (flag) {
            return R.ok();
        } else {
            return R.error();
        }
    }

    @ApiOperation(value = "批量删除医院设置")
    @DeleteMapping("/batchDelete")
    public R batchDelete(@RequestBody List<Long> idList) {
        hospitalSetService.removeByIds(idList);
        return R.ok();
    }

    @ApiOperation(value = "医院设置锁定和解锁")
    @PutMapping("/lockHospitalSet/{id}/{status}")
    public R lockHospitalSet(
            @PathVariable Long id,
            @PathVariable Integer status) {
        //根据id查询医院设置信息
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        //设置状态
        hospitalSet.setStatus(status);
        //更新医院设置状态
        hospitalSetService.updateById(hospitalSet);
        return R.ok();
    }


}

