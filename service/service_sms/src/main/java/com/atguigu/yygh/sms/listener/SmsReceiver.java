package com.atguigu.yygh.sms.listener;

import com.atguigu.mq.MqConst;
import com.atguigu.vo.msm.MsmVo;
import com.atguigu.yygh.sms.service.SmsService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class SmsReceiver {

    @Autowired
    private SmsService smsService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_MSM_ITEM, durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_MSM),
            key = MqConst.ROUTING_MSM_ITEM
    ))
    public void sendMessage(MsmVo msmVo, Message message, Channel channel) {
        //验证码
        if (msmVo.getPhone() != null) {
            smsService.subMitMessage(msmVo);
        }
        if (msmVo.getParam() != null) {
            smsService.remind(msmVo);
        }

    }
}
