package com.atguigu.yygh.order.controller;

import com.atguigu.yygh.exception.YYGHException;
import com.atguigu.yygh.order.service.PaymentService;
import com.atguigu.yygh.order.service.WeixinService;
import com.atguigu.yygh.result.R;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author Xu
 * @date 2022/4/28 11:18
 * yygh_parent com.atguigu.yygh.order.controller
 */
@RestController
@RequestMapping("/api/orderInfo/weixin")
public class WeixinController {

    @Autowired
    private WeixinService weixinService;

    @Autowired
    private PaymentService paymentService;

    @ApiOperation(value = "生成二维码地址")
    @GetMapping("/createNative/{orderId}")
    public R createNative(@PathVariable Long orderId) {

        String url = weixinService.createNative(orderId);
        return R.ok().data("codeUrl", url);
    }
    @ApiOperation(value = "向微信服务器查询是否完成支付")
    @GetMapping("/queryPayStatus/{orderId}")
    public R queryPayStatus(@PathVariable Long orderId) {
        Map<String, String> map = weixinService.queryPayStatus(orderId);
        if (map == null) {
            throw new YYGHException(2001, "支付失败");
        }
        if ("SUCCESS".equals(map.get("trade_state"))) {
            //付款成功 :更新订单orderInfo中支付的状态和支付记录表中的状态
            paymentService.updatePay(orderId, map);
            return R.ok();
        }
        return R.ok().message("支付中");
    }

}
