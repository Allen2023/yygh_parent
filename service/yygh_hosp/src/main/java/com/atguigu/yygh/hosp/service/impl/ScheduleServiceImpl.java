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
            throw new YYGHException(20002, "???????????????");
        }
        mongoSchedule.setIsDeleted(1);
        scheduleRepository.save(mongoSchedule);
    }

    @Override
    public Map<String, Object> getSchedulePage(String hoscode, String depcode, Integer page, Integer limit) {
        Map<String, Object> map = new HashMap<>();
        //????????????????????????????????????????????????
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);
        Aggregation aggregation = Aggregation.newAggregation(
                //????????????
                Aggregation.match(criteria),
                Aggregation.group("workDate")
                        //????????????????????????
                        .first("workDate").as("workDate")
                        .count().as("docCount")
                        //??????????????????????????????????????????????????????
                        .sum("reservedNumber").as("reservedNumber")
                        .sum("availableNumber").as("availableNumber"),
                Aggregation.sort(Sort.Direction.ASC, "workDate"),
                Aggregation.skip((page - 1) * limit),
                Aggregation.limit(limit)
        );
        //?????????????????????
        AggregationResults<BookingScheduleRuleVo> aggregationResults1 = mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);
        //?????????????????????
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = aggregationResults1.getMappedResults();
        //??????????????????????????????
        for (BookingScheduleRuleVo bookingScheduleRuleVo : bookingScheduleRuleVoList) {
            Date workDate = bookingScheduleRuleVo.getWorkDate();
            String dayOfWeek = this.getDayOfWeek(new DateTime(workDate));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
        }

        //??????????????????
        Criteria criteria1 = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);
        Aggregation totalAgg = Aggregation.newAggregation(
                Aggregation.match(criteria1),
                Aggregation.group("workDate")
                //????????????????????????????????????????????????
        );
        AggregationResults<BookingScheduleRuleVo> aggregationResults2 = mongoTemplate.aggregate(totalAgg, Schedule.class, BookingScheduleRuleVo.class);

        //??????????????????
        int total = aggregationResults2.getMappedResults().size();

        map.put("bookingScheduleRuleList", bookingScheduleRuleVoList);
        map.put("total", total);

        Map<String, String> baseMap = new HashMap<>();
        //??????????????????
        Hospital hospital = hospitalService.getByHoscode(hoscode);
        //??????????????????
        baseMap.put("hosname", hospital.getHosname());
        map.put("baseMap", baseMap);
        return map;
    }


    /**
     * ??????????????????????????????
     *
     * @param dateTime
     * @return
     */
    private String getDayOfWeek(DateTime dateTime) {
        String dayOfWeek = "";
        switch (dateTime.getDayOfWeek()) {
            case DateTimeConstants.SUNDAY:
                dayOfWeek = "??????";
                break;
            case DateTimeConstants.MONDAY:
                dayOfWeek = "??????";
                break;
            case DateTimeConstants.TUESDAY:
                dayOfWeek = "??????";
                break;
            case DateTimeConstants.WEDNESDAY:
                dayOfWeek = "??????";
                break;
            case DateTimeConstants.THURSDAY:
                dayOfWeek = "??????";
                break;
            case DateTimeConstants.FRIDAY:
                dayOfWeek = "??????";
                break;
            case DateTimeConstants.SATURDAY:
                dayOfWeek = "??????";
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

    //???????????????,????????????,????????????map?????????
    private void packageSchedule(Schedule schedule) {
        schedule.getParam().put("hosname", hospitalService.getByHoscode(schedule.getHoscode()).getHosname());
        schedule.getParam().put("depname", departmentService.findByHoscodeAndDepcode(schedule.getHoscode(), schedule.getDepcode()).getDepname());
        schedule.getParam().put("dayOfWeek", this.getDayOfWeek(new DateTime(schedule.getWorkDate())));
    }

    @Override
    public Map<String, Object> getBookingScheduleRule(Integer page, Integer limit, String hoscode, String depcode) {
        Hospital hospital = hospitalService.getByHoscode(hoscode);
        if (hospital == null) {
            throw new YYGHException(2001, "?????????????????????!");
        }
        //???????????????????????????
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Date> currentPage = this.getDateList(page, limit, hospital);
        //???????????????????????????
        List<Date> currentPageRecords = currentPage.getRecords();

        //????????????
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode).and("workDate").in(currentPageRecords)),
                Aggregation.group("workDate").first("workDate").as("workDate")
                        .count().as("docCount")
                        .sum("reservedNumber").as("reservedNumber")
                        .sum("availableNumber").as("availableNumber"),
                Aggregation.sort(Sort.Direction.ASC, "workDate"));

        //schedule????????????????????????
        AggregationResults<BookingScheduleRuleVo> aggregate = mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);
        //???????????????????????????????????????????????????
        List<BookingScheduleRuleVo> mappedResults = aggregate.getMappedResults();
        //??????????????????map ??????workDate ??????BookingScheduleRuleVo
        Map<Date, BookingScheduleRuleVo> booingScheduleRuleMap = mappedResults.stream().collect(Collectors.toMap(BookingScheduleRuleVo::getWorkDate, BookingScheduleRuleVo -> BookingScheduleRuleVo));
        int total = currentPageRecords.size();
        //?????????????????????????????????????????????????????????
        List<BookingScheduleRuleVo> bookingScheduleList = new ArrayList<>();
        //???????????????????????? ???BookingScheduleRuleVo??????
        for (int i = 0; i < total; i++) {
            //???????????????????????? ????????????
            Date date = currentPageRecords.get(i);
            //?????????????????????????????????
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

            //"?????? 0????????? ,1??????????????? ,-1????????????????????????"
            bookingScheduleRuleVo.setStatus(0);

            //??????????????????
            if (page == 1 && i == 0) {
                String stopTime = hospital.getBookingRule().getStopTime();
                DateTime stopDateTime = this.getDateTime(date, stopTime);
                //???????????????????????????stopDateTime??????
                if (stopDateTime.isBeforeNow()) {
                    //"?????? 0????????? ,1??????????????? ,-1????????????????????????"
                    bookingScheduleRuleVo.setStatus(-1);
                }
            }
            //???????????? ????????????
            if (page == currentPage.getPages() && i == total - 1) {
                bookingScheduleRuleVo.setStatus(1);
            }
            bookingScheduleList.add(bookingScheduleRuleVo);
        }

        Map<String, Object> stringObjectHashMap = new HashMap<>();
        //???????????????????????????????????????????????????map
        stringObjectHashMap.put("bookingScheduleList", bookingScheduleList);
        stringObjectHashMap.put("total", currentPage.getTotal());
        //??????????????????
        Map<String, String> baseMap = new HashMap<>();
        //????????????
        baseMap.put("hosname", hospitalService.getByHoscode(hoscode).getHosname());
        //??????
        Department department = departmentService.findByHoscodeAndDepcode(hoscode, depcode);
        //???????????????
        baseMap.put("bigname", department.getBigname());
        //????????????
        baseMap.put("depname", department.getDepname());
        //???
        baseMap.put("workDateString", new DateTime().toString("yyyy???MM???"));
        //????????????
        baseMap.put("releaseTime", hospital.getBookingRule().getReleaseTime());
        //????????????
        baseMap.put("stopTime", hospital.getBookingRule().getStopTime());
        stringObjectHashMap.put("baseMap", baseMap);
        return stringObjectHashMap;
    }

    private com.baomidou.mybatisplus.extension.plugins.pagination.Page<Date> getDateList(Integer page, Integer limit, Hospital hospital) {
        BookingRule bookingRule = hospital.getBookingRule();
        //??????????????????
        String releaseTime = bookingRule.getReleaseTime();
        //????????????????????????DateTime??????
        DateTime releaseDateTime = this.getDateTime(new Date(), releaseTime);
        //?????????????????? cycle=10
        Integer cycle = bookingRule.getCycle();
        //??????????????????????????????????????????true
        //isBeforeNow()???????????????????????????releaseDateTime??????
        //??????????????????????????????????????????????????????????????????????????????????????????1
        if (releaseDateTime.isBeforeNow()) {
            cycle = cycle + 1;
        }
        //????????????????????? ????????????????????????????????????
        List<Date> dateList = new ArrayList<>();
        for (int i = 0; i < cycle; i++) {
            //????????????????????????
            DateTime dateTime = new DateTime().plusDays(i);
            //???????????????????????????yyyy-MM-dd
            String dateTimeString = dateTime.toString("yyyy-MM-dd");
            dateList.add(new DateTime(dateTimeString).toDate());
        }

        //?????????????????????????????????
        int start = (page - 1) * limit;
        int end = start + limit;
        if (end > dateList.size()) {
            //????????????end??????dateList.size(),?????????????????????dateList.size()
            end = dateList.size();
        }
        //???????????????????????????
        List<Date> finalDateList = new ArrayList<>();
        for (int i = start; i < end; i++) {
            //???????????????????????????
            finalDateList.add(dateList.get(i));
        }
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Date> page1 = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, limit, dateList.size());
        //???????????????????????????????????????
        page1.setRecords(finalDateList);
        return page1;
    }

    /**
     * ???Date?????????yyyy-MM-dd HH:mm????????????DateTime
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
            throw new YYGHException(2001, "??????????????????");
        }
        //????????????????????????
        Hospital hospital = hospitalService.getByHoscode(schedule.getHoscode());
        if (null == hospital) {
            throw new YYGHException(2001, "??????????????????");
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
        //?????????????????????????????????????????????-1????????????0???
        Integer quitDay = bookingRule.getQuitDay();
        DateTime quitTime = this.getDateTime(new DateTime(schedule.getWorkDate()).plusDays(quitDay).toDate(), bookingRule.getQuitTime());
        scheduleOrderVo.setQuitTime(quitTime.toDate());

        //??????????????????
        DateTime releaseTime = this.getDateTime(scheduleOrderVo.getReserveDate(), bookingRule.getReleaseTime());
        scheduleOrderVo.setStartTime(releaseTime.toDate());

        //????????????????????????
        DateTime stopTime = this.getDateTime(schedule.getWorkDate(), bookingRule.getStopTime());
        scheduleOrderVo.setStopTime(stopTime.toDate());

        //??????????????????
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
