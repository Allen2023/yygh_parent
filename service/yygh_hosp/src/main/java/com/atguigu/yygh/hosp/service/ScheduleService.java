package com.atguigu.yygh.hosp.service;

import com.atguigu.model.hosp.Schedule;
import com.atguigu.vo.hosp.ScheduleOrderVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * @author Xu
 * @date 2022/4/19 19:23
 * yygh_parent com.atguigu.yygh.hosp.service
 */

public interface ScheduleService {
    void save(Map<String, Object> paramMap);

    Page<Schedule> getScheduleList(Map<String, Object> paramMap);

    void remove(Map<String, Object> paramMap);


    Map<String, Object> getSchedulePage(String hoscode, String depcode, Integer page, Integer limit);

    List<Schedule> getScheduleDetail(String hoscode, String depcode, String workDate);

    Map<String, Object> getBookingScheduleRule(Integer page, Integer limit, String hoscode, String depcode);


    Schedule getSchedule(String id);


    ScheduleOrderVo getScheduleOrderVo(String scheduleId);


    Boolean update(String scheduleId, Integer availableNumber);

}
