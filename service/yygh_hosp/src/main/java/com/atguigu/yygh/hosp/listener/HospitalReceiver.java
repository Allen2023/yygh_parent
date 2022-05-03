package com.atguigu.yygh.hosp.listener;

import com.atguigu.model.hosp.Schedule;
import com.atguigu.mq.MqConst;
import com.atguigu.mq.RabbitService;
import com.atguigu.vo.msm.MsmVo;
import com.atguigu.vo.order.OrderMqVo;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class HospitalReceiver {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private RabbitService rabbitService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_ORDER, durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_ORDER),
            key = MqConst.ROUTING_ORDER
    ))
    public void receiver(OrderMqVo orderMqVo, Message message, Channel channel) throws IOException {
        if (orderMqVo.getAvailableNumber() != null) {
            //下单成功更新预约数
            String scheduleId = orderMqVo.getScheduleId();
            Integer availableNumber = orderMqVo.getAvailableNumber();
           scheduleService.update(scheduleId, availableNumber);
        } else {
            //取消预约剩余预约数+1
            Schedule schedule = scheduleService.getSchedule(orderMqVo.getScheduleId());
            String scheduleId = orderMqVo.getScheduleId();
            int availableNumber = schedule.getAvailableNumber() + 1;
            schedule.setAvailableNumber(availableNumber);
            scheduleService.update(scheduleId, availableNumber);
        }
        //发送短信
        MsmVo msmVo = orderMqVo.getMsmVo();
        if(msmVo != null) {
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_MSM, MqConst.ROUTING_MSM_ITEM, msmVo);
        }

    }

}