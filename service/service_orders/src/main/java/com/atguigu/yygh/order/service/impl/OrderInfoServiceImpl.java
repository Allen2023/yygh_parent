package com.atguigu.yygh.order.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.atguigu.enums.OrderStatusEnum;
import com.atguigu.enums.PaymentStatusEnum;
import com.atguigu.model.order.OrderInfo;
import com.atguigu.model.order.PaymentInfo;
import com.atguigu.model.user.Patient;
import com.atguigu.mq.MqConst;
import com.atguigu.mq.RabbitService;
import com.atguigu.vo.hosp.ScheduleOrderVo;

import com.atguigu.vo.msm.MsmVo;
import com.atguigu.vo.order.OrderCountQueryVo;
import com.atguigu.vo.order.OrderCountVo;
import com.atguigu.vo.order.OrderMqVo;
import com.atguigu.vo.order.OrderQueryVo;
import com.atguigu.yygh.exception.YYGHException;
import com.atguigu.yygh.hosp.client.SchduleFeignClient;
import com.atguigu.yygh.order.mapper.OrderInfoMapper;
import com.atguigu.yygh.order.service.OrderInfoService;

import com.atguigu.yygh.order.service.PaymentService;
import com.atguigu.yygh.order.service.WeixinService;
import com.atguigu.yygh.order.utils.HttpClient;
import com.atguigu.yygh.order.utils.HttpRequestHelper;
import com.atguigu.yygh.user.client.PatientFeignClient;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2022-04-27
 */
@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {

    @Autowired
    private SchduleFeignClient SchduleFeignClient;

    @Autowired
    private PatientFeignClient patientFeignClient;

    @Autowired
    private RabbitService rabbitService;

    @Autowired
    private WeixinService weixinService;

    @Autowired
    private PaymentService paymentService;

    @Override
    public Long saveOrder(String scheduleId, Long patientId) {
        //1.scheduleId查询排班数据
        ScheduleOrderVo scheduleOrderVo = SchduleFeignClient.getScheduleInfo(scheduleId);
        //判断当前时间是否超过停止预约时间超过则抛异常
        Date stopTime = scheduleOrderVo.getStopTime();

        if (new DateTime(stopTime).isBeforeNow()) {
            throw new YYGHException(20001, "超过了当天挂号截止时间");
        }
        //2.patientId查询就诊人数据
        Patient patient = patientFeignClient.getPatientInfo(patientId);
        //3.平台请求第三方医院获取返回的数据
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("hoscode", scheduleOrderVo.getHoscode());
        paramMap.put("depcode", scheduleOrderVo.getDepcode());
        paramMap.put("hosScheduleId", scheduleOrderVo.getHosScheduleId());
        paramMap.put("reserveDate", scheduleOrderVo.getReserveDate());
        paramMap.put("reserveTime", scheduleOrderVo.getReserveTime());
        paramMap.put("amount", scheduleOrderVo.getAmount());
        //HttpClient client = new HttpClient("http://localhost:9998/order/submitOrder", paramMap);
        JSONObject jsonObject = HttpRequestHelper.sendRequest(paramMap, "http://localhost:9998/order/submitOrder");

        if ((jsonObject != null) && (jsonObject.getInteger("code") == 200)) {
            OrderInfo orderInfo = new OrderInfo();
            //订单号
            String outTradeNo = System.currentTimeMillis() + "" + new Random().nextInt(100);
            orderInfo.setOutTradeNo(outTradeNo);
            orderInfo.setScheduleId(scheduleOrderVo.getHosScheduleId());
            orderInfo.setUserId(patient.getUserId());
            orderInfo.setPatientId(patientId);
            orderInfo.setPatientName(patient.getName());
            orderInfo.setPatientPhone(patient.getPhone());
            orderInfo.setOrderStatus(OrderStatusEnum.UNPAID.getStatus());
            orderInfo.setReserveDate(scheduleOrderVo.getReserveDate());
            orderInfo.setReserveTime(scheduleOrderVo.getReserveTime());
            orderInfo.setQuitTime(scheduleOrderVo.getQuitTime());
            orderInfo.setAmount(scheduleOrderVo.getAmount());
            orderInfo.setHoscode(scheduleOrderVo.getHoscode());
            orderInfo.setDepcode(scheduleOrderVo.getDepcode());
            orderInfo.setHosname(scheduleOrderVo.getHosname());
            orderInfo.setDepname(scheduleOrderVo.getDepname());
            orderInfo.setTitle(scheduleOrderVo.getTitle());


            //3.2平台请求第三方医院获取返回的数据成功
            JSONObject data = jsonObject.getJSONObject("data");
            //预约记录唯一标识（医院预约记录主键）
            String hosRecordId = data.getString("hosRecordId");
            //预约序号
            Integer number = data.getInteger("number");
            //取号时间
            String fetchTime = data.getString("fetchTime");
            //取号地址
            String fetchAddress = data.getString("fetchAddress");

            //设置添加数据--医院接口返回数据
            orderInfo.setHosRecordId(hosRecordId);
            orderInfo.setNumber(number);
            orderInfo.setFetchTime(fetchTime);
            orderInfo.setFetchAddress(fetchAddress);

            //3.2.2将三部分数据保存到order_info表
            baseMapper.insert(orderInfo);

            //3.2.3把平台剩余可预约数更新
            // 根据医院返回数据，更新排班数量
            //排班可预约数
            Integer reservedNumber = data.getInteger("reservedNumber");
            //排班剩余预约数
            Integer availableNumber = data.getInteger("availableNumber");

            //发送mq信息更新号源和短信通知
            OrderMqVo orderMqVo = new OrderMqVo();
            orderMqVo.setScheduleId(scheduleId);
            orderMqVo.setReservedNumber(reservedNumber);
            orderMqVo.setAvailableNumber(availableNumber);

            MsmVo msmVo = new MsmVo();
            msmVo.setPhone(patient.getPhone());
            String reserveDate =
                    new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd")
                            + (orderInfo.getReserveTime() == 0 ? "上午" : "下午");
            Map<String, Object> param = new HashMap<String, Object>() {{
                put("title", orderInfo.getHosname() + "|" + orderInfo.getDepname() + "|" + orderInfo.getTitle());
                put("amount", orderInfo.getAmount());
                put("reserveDate", reserveDate);
                put("name", orderInfo.getPatientName());
                put("quitTime", new DateTime(orderInfo.getQuitTime()).toString("yyyy-MM-dd HH:mm"));
            }};
            msmVo.setParam(param);
            orderMqVo.setMsmVo(msmVo);
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER, MqConst.ROUTING_ORDER, orderMqVo);

            //3.2.4预约成功 返回一条预约成功的短信提示

            //4.给前端返回一个orderID
            return orderInfo.getId();
        } else {
            //3.1平台请求第三方医院获取返回的数据不成功 返回异常信息
            System.out.println("下单失败");
            throw new YYGHException(20001, "返回数据失败");
        }
    }

    @Override
    public Page<OrderInfo> getPageList(Integer page, Integer limit, OrderQueryVo orderQueryVo) {
        //orderQueryVo获取条件值
        Long userId = orderQueryVo.getUserId();
        String name = orderQueryVo.getKeyword(); //医院名称
        Long patientId = orderQueryVo.getPatientId(); //就诊人名称
        String orderStatus = orderQueryVo.getOrderStatus(); //订单状态
        String reserveDate = orderQueryVo.getReserveDate();//安排时间
        String createTimeBegin = orderQueryVo.getCreateTimeBegin();
        String createTimeEnd = orderQueryVo.getCreateTimeEnd();

        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        if (!StringUtils.isEmpty(name)) {
            wrapper.like("hosname", name);
        }
        if (!StringUtils.isEmpty(patientId)) {
            wrapper.eq("patient_id", patientId);
        }
        if (!StringUtils.isEmpty(orderStatus)) {
            wrapper.eq("order_status", orderStatus);
        }
        if (!StringUtils.isEmpty(reserveDate)) {
            wrapper.ge("reserve_date", reserveDate);
        }
        if (!StringUtils.isEmpty(createTimeBegin)) {
            wrapper.ge("create_time", createTimeBegin);
        }
        if (!StringUtils.isEmpty(createTimeEnd)) {
            wrapper.le("create_time", createTimeEnd);
        }
        Page<OrderInfo> orderInfoPage = new Page<>(page, limit);
        baseMapper.selectPage(orderInfoPage, wrapper);
        orderInfoPage.getRecords().forEach(this::packOrderInfo);
        return orderInfoPage;
    }

    private void packOrderInfo(OrderInfo item) {
        item.getParam().put("orderStatusString", OrderStatusEnum.getStatusNameByStatus(item.getOrderStatus()));
    }


    @Override
    public OrderInfo getOrders(Long orderId, Long userId) {
        OrderInfo orderInfo = baseMapper.selectById(orderId);
        this.packOrderInfo(orderInfo);
        return orderInfo;
    }

    @Override
    public void cancelOrder(Long orderId) {
        OrderInfo orderInfo = this.getById(orderId);
        //如果当前时间大约退号时间,不能取消预约
        DateTime quitTime = new DateTime(orderInfo.getQuitTime());
        if (quitTime.isBeforeNow()) {
            throw new YYGHException(2001, "已过最后退号时间");
        }
        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("hoscode", orderInfo.getHoscode());
        reqMap.put("hosRecordId", orderInfo.getHosRecordId());
        reqMap.put("timestamp", HttpRequestHelper.getTimestamp());
        reqMap.put("sign", "");
        JSONObject result = HttpRequestHelper.sendRequest(reqMap, "http://localhost:9998/order/updateCancelStatus");
        if (result.getInteger("code") != 200) {
            throw new YYGHException(2001, "取消预约失败");
        } else {
            //是否已经支付 退款
            if (orderInfo.getOrderStatus().intValue() == OrderStatusEnum.PAID.getStatus().intValue()) {
                //已支付 退款
                boolean isRefund = weixinService.refund(orderId);
                if (!isRefund) {
                    throw new YYGHException(2001, "退款失败");
                }
            }
            //更改订单状态为取消
            orderInfo.setOrderStatus(OrderStatusEnum.CANCLE.getStatus());
            this.updateById(orderInfo);
            //更新支付记录状态
            PaymentInfo paymentInfo = paymentService.getOne(new LambdaQueryWrapper<PaymentInfo>().eq(PaymentInfo::getOrderId, orderId));
            paymentInfo.setPaymentStatus(-1);
            paymentInfo.setUpdateTime(new Date());
            paymentService.updateById(paymentInfo);

            //发送mq信息更新预约数 我们与下单成功更新预约数使用相同的mq信息，不设置可预约数与剩余预约数，接收端可预约数减1即可
            OrderMqVo orderMqVo = new OrderMqVo();
            orderMqVo.setScheduleId(orderInfo.getScheduleId());
            //短信提示
            MsmVo msmVo = new MsmVo();
            msmVo.setPhone(orderInfo.getPatientPhone());
            orderMqVo.setMsmVo(msmVo);
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER, MqConst.ROUTING_ORDER, orderMqVo);

        }
    }

    @Override
    public void remind() {

        List<OrderInfo> orderInfoList = baseMapper.selectList(
                new LambdaQueryWrapper<OrderInfo>().eq(OrderInfo::getReserveDate, new DateTime().toString("yyyy-MM-dd")).ne(OrderInfo::getOrderStatus, OrderStatusEnum.CANCLE)
        );
        for (OrderInfo orderInfo : orderInfoList) {
            MsmVo msmVo = new MsmVo();
            String reserveDate = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd") + (orderInfo.getReserveTime() == 0 ? "上午" : "下午");
            Map<String, Object> param = new HashMap<String, Object>() {{
                put("title", orderInfo.getHosname() + "|" + orderInfo.getDepname() + "|" + orderInfo.getTitle());
                put("reserveDate", reserveDate);
                put("name", orderInfo.getPatientName());
            }};
            msmVo.setParam(param);
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_MSM, MqConst.ROUTING_MSM_ITEM, msmVo);
        }
    }

    @Override
    public Map<String, Object> getCountMap(OrderCountQueryVo orderCountQueryVo) {
        Map<String, Object> map = new HashMap<>();
        List<OrderCountVo> orderCountVoList = baseMapper.selectOrderCount(orderCountQueryVo);
        //将orderCountVoList中的reserveDate取出并添加到dateList中
        //日期列表
        List<String> dateList = orderCountVoList.stream().map(OrderCountVo::getReserveDate).collect(Collectors.toList());
        //将orderCountVoList中的count取出并添加到countList中
        //订单统计列表
        List<Integer> countList = orderCountVoList.stream().map(OrderCountVo::getCount).collect(Collectors.toList());
        map.put("dateList", dateList);
        map.put("countList", countList);
        return map;
    }
}
