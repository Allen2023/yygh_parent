package com.atguigu.yygh.hosp.client;

import com.atguigu.vo.hosp.ScheduleOrderVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author Xu
 * @date 2022/4/27 23:01
 * yygh_parent com.atguigu.yygh.hosp.client
 */
@FeignClient("yygh-hosp")
public interface SchduleFeignClient {
    @ApiOperation(value = "远程获取排班数据")
    @GetMapping("/user/hosp/hospital/remote/{scheduleId}")
    public ScheduleOrderVo getScheduleInfo(@PathVariable("scheduleId") String scheduleId);
}
