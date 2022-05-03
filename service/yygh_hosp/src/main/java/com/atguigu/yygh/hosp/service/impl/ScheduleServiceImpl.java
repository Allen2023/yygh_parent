package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.model.hosp.BookingRule;
import com.atguigu.model.hosp.Department;
import com.atguigu.model.hosp.Hospital;
import com.atguigu.vo.hosp.BookingScheduleRuleVo;
import com.atguigu.vo.hosp.ScheduleOrderVo;
import com.atguigu.yygh.exception.YYGHException;
import com.atguigu.yygh.hosp.repository.ScheduleRepository;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.model.hosp.Schedule;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;

import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;


import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Xu
 * @date 2022/4/19 19:23
 * yygh_parent com.atguigu.yygh.hosp.service.impl
 */
@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private DepartmentService departmentService;

    @Override
    public void save(Map<String, Object> paramMap) {
        Schedule schedule = JSONObject.parseObject(JSONObject.toJSONString(paramMap), Schedule.class);
        String hoscode = schedule.getHoscode();
        String depcode = schedule.getDepcode();
        String hosScheduleId = schedule.getHosScheduleId();
        Schedule mongoSchedule = scheduleRepository.findByHoscodeAndDepcodeAndHosScheduleId(hoscode, depcode, hosScheduleId);

        if (mongoSchedule == null) {
            schedule.setCreateTime(new Date());
            schedule.setUpdateTime(new Date());
            schedule.setIsDeleted(0);
            scheduleRepository.save(schedule);
        } else {
            schedule.setCreateTime(mongoSchedule.getCreateTime());
            schedule.setUpdateTime(new Date());
            schedule.setIsDeleted(mongoSchedule.getIsDeleted());
            schedule.setId(mongoSchedule.getId());
            scheduleRepository.save(schedule);
        }
    }

    @Override
    public Page<Schedule> getScheduleList(Map<String, Object> paramMap) {

        String hoscode = (String) paramMap.get("hoscode");
        String pageNum = (String) paramMap.get("page");
        String pageSize = (String) paramMap.get("limit");
        Schedule schedule = new Schedule();
        schedule.setHoscode(hoscode);
        schedule.setIsDeleted(0);
        Example<Schedule> scheduleExample = Example.of(schedule);
        PageRequest pageRequest = PageRequest.of(Integer.parseInt(pageNum) - 1, Integer.parseInt(pageSize));

        return scheduleRepository.findAll(scheduleExample, pageRequest);
    }

    @Override
    public void remove(Map<String, Object> paramMap) {
        String hoscode = (String) paramMap.get("hoscode");
        String hosScheduleId = (String) paramMap.get("hosScheduleId");
        Schedule schedule = new Schedule();
        schedule.setHoscode(hoscode);
        schedule.setHosScheduleId(hosScheduleId);

        Schedule mongoSchedule = scheduleRepository.findByHoscodeAndHosScheduleId(hoscode, hosScheduleId);
        if (mongoSchedule == null) {
            throw new YYGHException(20002, "排班不存在");
        }
        mongoSchedule.setIsDeleted(1);
        scheduleRepository.save(mongoSchedule);
    }

    @Override
    public Map<String, Object> getSchedulePage(String hoscode, String depcode, Integer page, Integer limit) {
        Map<String, Object> map = new HashMap<>();
        //根据医院编号科室编号查询当前科室
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);
        Aggregation aggregation = Aggregation.newAggregation(
                //匹配条件
                Aggregation.match(criteria),
                Aggregation.group("workDate")
                        //根据工作日期分组
                        .first("workDate").as("workDate")
                        .count().as("docCount")
                        //对总预约人数和总剩余预约人数进行求和
                        .sum("reservedNumber").as("reservedNumber")
                        .sum("availableNumber").as("availableNumber"),
                Aggregation.sort(Sort.Direction.ASC, "workDate"),
                Aggregation.skip((page - 1) * limit),
                Aggregation.limit(limit)
        );
        //获取当前页数据
        AggregationResults<BookingScheduleRuleVo> aggregationResults1 = mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);
        //获取当前页数据
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = aggregationResults1.getMappedResults();
        //将工作日期转化为星期
        for (BookingScheduleRuleVo bookingScheduleRuleVo : bookingScheduleRuleVoList) {
            Date workDate = bookingScheduleRuleVo.getWorkDate();
            String dayOfWeek = this.getDayOfWeek(new DateTime(workDate));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
        }

        //获取总记录数
        Criteria criteria1 = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);
        Aggregation totalAgg = Aggregation.newAggregation(
                Aggregation.match(criteria1),
                Aggregation.group("workDate")
                //根据工作日期进行分组获取总记录数
        );
        AggregationResults<BookingScheduleRuleVo> aggregationResults2 = mongoTemplate.aggregate(totalAgg, Schedule.class, BookingScheduleRuleVo.class);

        //获取总记录数
        int total = aggregationResults2.getMappedResults().size();

        map.put("bookingScheduleRuleList", bookingScheduleRuleVoList);
        map.put("total", total);

        Map<String, String> baseMap = new HashMap<>();
        //获取医院名称
        Hospital hospital = hospitalService.getByHoscode(hoscode);
        //其他基础数据
        baseMap.put("hosname", hospital.getHosname());
        map.put("baseMap", baseMap);
        return map;
    }


    /**
     * 根据日期获取周几数据
     *
     * @param dateTime
     * @return
     */
    private String getDayOfWeek(DateTime dateTime) {
        String dayOfWeek = "";
        switch (dateTime.getDayOfWeek()) {
            case DateTimeConstants.SUNDAY:
                dayOfWeek = "周日";
                break;
            case DateTimeConstants.MONDAY:
                dayOfWeek = "周一";
                break;
            case DateTimeConstants.TUESDAY:
                dayOfWeek = "周二";
                break;
            case DateTimeConstants.WEDNESDAY:
                dayOfWeek = "周三";
                break;
            case DateTimeConstants.THURSDAY:
                dayOfWeek = "周四";
                break;
            case DateTimeConstants.FRIDAY:
                dayOfWeek = "周五";
                break;
            case DateTimeConstants.SATURDAY:
                dayOfWeek = "周六";
            default:
                break;
        }
        return dayOfWeek;
    }


    @Override
    public List<Schedule> getScheduleDetail(String hoscode, String depcode, String workDate) {
        List<Schedule> scheduleList = scheduleRepository.findByHoscodeAndDepcodeAndWorkDate(hoscode, depcode, new DateTime(workDate).toDate());
        scheduleList.forEach(this::packageSchedule);
        return scheduleList;
    }

    //将医院信息,科室信息,周几放入map数据中
    private void packageSchedule(Schedule schedule) {
        schedule.getParam().put("hosname", hospitalService.getByHoscode(schedule.getHoscode()).getHosname());
        schedule.getParam().put("depname", departmentService.findByHoscodeAndDepcode(schedule.getHoscode(), schedule.getDepcode()).getDepname());
        schedule.getParam().put("dayOfWeek", this.getDayOfWeek(new DateTime(schedule.getWorkDate())));
    }

    @Override
    public Map<String, Object> getBookingScheduleRule(Integer page, Integer limit, String hoscode, String depcode) {
        Hospital hospital = hospitalService.getByHoscode(hoscode);
        if (hospital == null) {
            throw new YYGHException(2001, "暂时无医院信息!");
        }
        //查询当前页时间列表
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Date> currentPage = this.getDateList(page, limit, hospital);
        //获得当前页时间列表
        List<Date> currentPageRecords = currentPage.getRecords();

        //聚合查询
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode).and("workDate").in(currentPageRecords)),
                Aggregation.group("workDate").first("workDate").as("workDate")
                        .count().as("docCount")
                        .sum("reservedNumber").as("reservedNumber")
                        .sum("availableNumber").as("availableNumber"),
                Aggregation.sort(Sort.Direction.ASC, "workDate"));

        //schedule集合查询排班信息
        AggregationResults<BookingScheduleRuleVo> aggregate = mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);
        //拿到带有当前时间列表的预约信息集合
        List<BookingScheduleRuleVo> mappedResults = aggregate.getMappedResults();
        //将集合映射成map 键为workDate 值为BookingScheduleRuleVo
        Map<Date, BookingScheduleRuleVo> booingScheduleRuleMap = mappedResults.stream().collect(Collectors.toMap(BookingScheduleRuleVo::getWorkDate, BookingScheduleRuleVo -> BookingScheduleRuleVo));
        int total = currentPageRecords.size();
        //经过下面处理获得当前时间的预约信息集合
        List<BookingScheduleRuleVo> bookingScheduleList = new ArrayList<>();
        //遍历当前时间列表 给BookingScheduleRuleVo赋值
        for (int i = 0; i < total; i++) {
            //遍历拿到当前时间 设置状态
            Date date = currentPageRecords.get(i);
            //拿到当前时间的预约信息
            BookingScheduleRuleVo bookingScheduleRuleVo = booingScheduleRuleMap.get(date);
            if (bookingScheduleRuleVo == null) {
                bookingScheduleRuleVo = new BookingScheduleRuleVo();
                bookingScheduleRuleVo.setDocCount(0);
                bookingScheduleRuleVo.setReservedNumber(0);
                bookingScheduleRuleVo.setAvailableNumber(-1);
                bookingScheduleRuleVo.setStatus(1);
            }
            bookingScheduleRuleVo.setWorkDate(date);
            bookingScheduleRuleVo.setDayOfWeek(this.getDayOfWeek(new DateTime(date)));

            //"状态 0：正常 ,1：即将放号 ,-1：当天已停止挂号"
            bookingScheduleRuleVo.setStatus(0);

            //第一页第一条
            if (page == 1 && i == 0) {
                String stopTime = hospital.getBookingRule().getStopTime();
                DateTime stopDateTime = this.getDateTime(date, stopTime);
                //判断当前时间是否在stopDateTime之前
                if (stopDateTime.isBeforeNow()) {
                    //"状态 0：正常 ,1：即将放号 ,-1：当天已停止挂号"
                    bookingScheduleRuleVo.setStatus(-1);
                }
            }
            //最后一页 最后一行
            if (page == currentPage.getPages() && i == total - 1) {
                bookingScheduleRuleVo.setStatus(1);
            }
            bookingScheduleList.add(bookingScheduleRuleVo);
        }

        Map<String, Object> stringObjectHashMap = new HashMap<>();
        //总页数和当前时间的预约信息集合放入map
        stringObjectHashMap.put("bookingScheduleList", bookingScheduleList);
        stringObjectHashMap.put("total", currentPage.getTotal());
        //其他基础数据
        Map<String, String> baseMap = new HashMap<>();
        //医院名称
        baseMap.put("hosname", hospitalService.getByHoscode(hoscode).getHosname());
        //科室
        Department department = departmentService.findByHoscodeAndDepcode(hoscode, depcode);
        //大科室名称
        baseMap.put("bigname", department.getBigname());
        //科室名称
        baseMap.put("depname", department.getDepname());
        //月
        baseMap.put("workDateString", new DateTime().toString("yyyy年MM月"));
        //放号时间
        baseMap.put("releaseTime", hospital.getBookingRule().getReleaseTime());
        //停号时间
        baseMap.put("stopTime", hospital.getBookingRule().getStopTime());
        stringObjectHashMap.put("baseMap", baseMap);
        return stringObjectHashMap;
    }

    private com.baomidou.mybatisplus.extension.plugins.pagination.Page<Date> getDateList(Integer page, Integer limit, Hospital hospital) {
        BookingRule bookingRule = hospital.getBookingRule();
        //获取放号时间
        String releaseTime = bookingRule.getReleaseTime();
        //将放号时间转化为DateTime类型
        DateTime releaseDateTime = this.getDateTime(new Date(), releaseTime);
        //获取挂号周期 cycle=10
        Integer cycle = bookingRule.getCycle();
        //如果放号时间在当前时间之前为true
        //isBeforeNow()比较当前时间是否在releaseDateTime之前
        //如果当天放号时间已过，则预约周期后一天为即将放号时间，周期加1
        if (releaseDateTime.isBeforeNow()) {
            cycle = cycle + 1;
        }
        //创建总时间列表 里面最大有一个周期的时间
        List<Date> dateList = new ArrayList<>();
        for (int i = 0; i < cycle; i++) {
            //遍历创建当前时间
            DateTime dateTime = new DateTime().plusDays(i);
            //将当前时间格式化为yyyy-MM-dd
            String dateTimeString = dateTime.toString("yyyy-MM-dd");
            dateList.add(new DateTime(dateTimeString).toDate());
        }

        //设置每页起始和结束数据
        int start = (page - 1) * limit;
        int end = start + limit;
        if (end > dateList.size()) {
            //最后一页end大于dateList.size(),就代表只能查到dateList.size()
            end = dateList.size();
        }
        //设置当前页事件列表
        List<Date> finalDateList = new ArrayList<>();
        for (int i = start; i < end; i++) {
            //只获取当前页的时间
            finalDateList.add(dateList.get(i));
        }
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Date> page1 = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, limit, dateList.size());
        //将当前页时间列表传入当前页
        page1.setRecords(finalDateList);
        return page1;
    }

    /**
     * 将Date日期（yyyy-MM-dd HH:mm）转换为DateTime
     */
    private DateTime getDateTime(Date date, String timeString) {
        String dateTimeString = new DateTime(date).toString("yyyy-MM-dd") + " " + timeString;
        DateTime dateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(dateTimeString);
        return dateTime;
    }


    @Override
    public Schedule getSchedule(String id) {
        Schedule schedule = scheduleRepository.findById((id)).get();
        this.packageSchedule(schedule);
        return schedule;
    }

    @Override
    public ScheduleOrderVo getScheduleOrderVo(String scheduleId) {
        ScheduleOrderVo scheduleOrderVo = new ScheduleOrderVo();
        Schedule schedule = scheduleRepository.findById((scheduleId)).get();
        BeanUtils.copyProperties(schedule, scheduleOrderVo);
        if (null == schedule) {
            throw new YYGHException(2001, "排班查询失败");
        }
        //获取预约规则信息
        Hospital hospital = hospitalService.getByHoscode(schedule.getHoscode());
        if (null == hospital) {
            throw new YYGHException(2001, "医院查询失败");
        }
       /* scheduleOrderVo.setHosScheduleId(schedule.getHosScheduleId());
        scheduleOrderVo.setHoscode(schedule.getHoscode());
        scheduleOrderVo.setDepcode(schedule.getDepcode());*/
        scheduleOrderVo.setDepname(departmentService.findByHoscodeAndDepcode(schedule.getHoscode(), schedule.getDepcode()).getDepname());
        scheduleOrderVo.setHosname(hospitalService.getByHoscode(schedule.getHoscode()).getHosname());
//        scheduleOrderVo.setAmount(schedule.getAmount());
        scheduleOrderVo.setReserveDate(schedule.getWorkDate());
        scheduleOrderVo.setReserveTime(schedule.getWorkTime());
        //scheduleOrderVo.setAvailableNumber(schedule.getAvailableNumber());
        //scheduleOrderVo.setTitle(schedule.getTitle());


        BookingRule bookingRule = hospital.getBookingRule();
        //退号截止天数（如：就诊前一天为-1，当天为0）
        Integer quitDay = bookingRule.getQuitDay();
        DateTime quitTime = this.getDateTime(new DateTime(schedule.getWorkDate()).plusDays(quitDay).toDate(), bookingRule.getQuitTime());
        scheduleOrderVo.setQuitTime(quitTime.toDate());

        //预约开始时间
        DateTime releaseTime = this.getDateTime(scheduleOrderVo.getReserveDate(), bookingRule.getReleaseTime());
        scheduleOrderVo.setStartTime(releaseTime.toDate());

        //当天停止挂号时间
        DateTime stopTime = this.getDateTime(schedule.getWorkDate(), bookingRule.getStopTime());
        scheduleOrderVo.setStopTime(stopTime.toDate());

        //预约截止时间
        DateTime endTime = this.getDateTime(new DateTime().plusDays(bookingRule.getCycle()).toDate(), bookingRule.getStopTime());
        scheduleOrderVo.setEndTime(endTime.toDate());

        return scheduleOrderVo;
    }

    @Override
    public Boolean update(String scheduleId, Integer availableNumber) {
        Schedule schedule = scheduleRepository.findById(scheduleId).get();
        schedule.setAvailableNumber(availableNumber);
        schedule.setUpdateTime(new Date());
        scheduleRepository.save(schedule);
        return true;
    }
}
