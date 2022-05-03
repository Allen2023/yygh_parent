package com.atguigu.yygh.order.listener;

import com.atguigu.mq.MqConst;
import com.atguigu.yygh.order.service.OrderInfoService;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Xu
 * @date 2022/4/29 10:13
 * yygh_parent com.atguigu.yygh.order.listener
 */
@Component
public class OrderReceiver {

    @Autowired
    private OrderInfoService orderInfoService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_TASK_8, durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_TASK),
            key = MqConst.ROUTING_TASK_8
    ))
    public void remind(Message message, Channel channel) {
        orderInfoService.remind();
    }
}

