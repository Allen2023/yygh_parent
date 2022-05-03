package com.atguigu.yygh.hosp.controller.api;

import com.atguigu.yygh.hosp.bean.Result;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.hosp.utils.HttpRequestHelper;
import com.atguigu.model.hosp.Schedule;
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
 * @date 2022/4/19 19:22
 * yygh_parent com.atguigu.yygh.hosp.controller.api
 */

@RestController
@RequestMapping("/api/hosp")
public class ApiScheduleController {
    @Autowired
    private ScheduleService scheduleService;

    @ApiOperation(value = "上传排班信息")
    @PostMapping("/saveSchedule")
    public Result saveSchedule(HttpServletRequest request) {
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        scheduleService.save(paramMap);
        return Result.ok();
    }

    @ApiOperation(value = "查询排班信息")
    @PostMapping("/schedule/list")
    public Result getScheduleList(HttpServletRequest request) {
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        Page<Schedule> page = scheduleService.getScheduleList(paramMap);
        return Result.ok(page);
    }

    @ApiOperation(value = "删除排班信息")
    @PostMapping("/schedule/remove")
    public Result removeSchedule(HttpServletRequest request) {
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        scheduleService.remove(paramMap);
        return Result.ok();
    }
}
