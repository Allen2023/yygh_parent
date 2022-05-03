package com.atguigu.yygh.task.remind;

import com.atguigu.mq.MqConst;
import com.atguigu.mq.RabbitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;



/**
 * @author Xu
 * @date 2022/4/29 9:24
 * yygh_parent com.atguigu.yygh.task.remind
 */
@Component
@EnableScheduling
public class ScheduledTask {
    @Autowired
    private RabbitService rabbitService;

    //@Scheduled(cron = "0/5 * * * * ?")
    public void remind() {
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK,MqConst.ROUTING_TASK_8,"xxx");
        //System.out.println(new Date());
    }
}
