package com.atguigu.yygh.hosp.controller.admin;

import com.atguigu.model.hosp.Schedule;
import com.atguigu.vo.hosp.ScheduleOrderVo;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.result.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author Xu
 * @date 2022/4/21 14:35
 * yygh_parent com.atguigu.yygh.hosp.controller.admin
 */
@RestController
@RequestMapping("/admin/hosp/schedule")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    //获取排班分页信息
    @GetMapping("/getScheduleRule/{hoscode}/{depcode}/{page}/{limit}")
    public R getSchedulePage(@PathVariable String hoscode, @PathVariable String depcode,
                             @PathVariable Integer page, @PathVariable Integer limit) {
        Map<String,Object> map = scheduleService.getSchedulePage(hoscode, depcode, page, limit);
        return R.ok().data(map);
    }

    @GetMapping("/getScheduleDetail/{hoscode}/{depcode}/{workDate}")
    public R getScheduleDetail(@PathVariable String hoscode, @PathVariable String depcode,
                             @PathVariable String workDate) {
        List<Schedule> getScheduleDetaiList = scheduleService.getScheduleDetail(hoscode, depcode,workDate);
        return R.ok().data("list",getScheduleDetaiList);
    }
}
