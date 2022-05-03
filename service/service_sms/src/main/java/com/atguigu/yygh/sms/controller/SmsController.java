package com.atguigu.yygh.sms.controller;

import com.atguigu.yygh.result.R;
import com.atguigu.yygh.sms.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/sms")
public class SmsController {



    @Autowired
    private SmsService smsService;


    @PostMapping(value = "/send/{phone}")
    public R send(@PathVariable String phone) {
        boolean flag = smsService.send(phone);
        if (flag) {
            return R.ok();
        } else {
            return R.error();
        }
    }
}