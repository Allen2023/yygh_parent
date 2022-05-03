package com.atguigu.yygh.hosp.controller.user;

import com.atguigu.model.hosp.Schedule;
import com.atguigu.vo.hosp.ScheduleOrderVo;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.result.R;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author Xu
 * @date 2022/4/26 20:43
 * yygh_parent com.atguigu.yygh.hosp.controller.user
 */
@RestController
@RequestMapping("/user/hosp/hospital")
public class UserScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @ApiOperation(value = "获取可预约排班数据")
    @GetMapping("/auth/getBookingScheduleRule/{page}/{limit}/{hoscode}/{depcode}")
    public R getBookingScheduleRule(@PathVariable Integer page, @PathVariable Integer limit,
                                    @PathVariable String hoscode, @PathVariable String depcode) {
        Map<String, Object> map = scheduleService.getBookingScheduleRule(page, limit, hoscode, depcode);
        return R.ok().data(map);
    }

    @ApiOperation(value = "获取排班数据")
    @GetMapping("/auth/findScheduleList/{hoscode}/{depcode}/{workDate}")
    public R findScheduleList(@PathVariable String hoscode, @PathVariable String depcode, @PathVariable String workDate) {
        List<Schedule> scheduleList = scheduleService.getScheduleDetail(hoscode, depcode, workDate);
        return R.ok().data("scheduleList", scheduleList);
    }

    @ApiOperation(value = "获取排班数据")
    @GetMapping("/getSchedule/{id}")
    public R getSchedule(@PathVariable String id) {
        Schedule schedule = scheduleService.getSchedule(id);
        return R.ok().data("schedule", schedule);
    }


    @ApiOperation(value = "远程获取排班数据")
    @GetMapping("/remote/{scheduleId}")
    public ScheduleOrderVo getScheduleInfo(@PathVariable("scheduleId") String scheduleId) {
        ScheduleOrderVo scheduleOrderVo = scheduleService.getScheduleOrderVo(scheduleId);
        return scheduleOrderVo;
    }
}
