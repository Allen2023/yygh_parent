package com.atguigu.yygh.order.controller;


import com.atguigu.enums.OrderStatusEnum;
import com.atguigu.model.hosp.Schedule;
import com.atguigu.model.order.OrderInfo;
import com.atguigu.vo.order.OrderCountQueryVo;
import com.atguigu.vo.order.OrderQueryVo;
import com.atguigu.yygh.order.service.OrderInfoService;
import com.atguigu.yygh.result.R;
import com.atguigu.yygh.utils.JwtHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 订单表 前端控制器
 * </p>
 *
 * @author atguigu
 * @since 2022-04-27
 */
@Api(tags = "订单接口")
@RestController
@RequestMapping("/api/orderInfo/")
public class OrderInfoController {
    // /api/orderInfo/submitOrder/62682d97a6071a15e49d1836/9"
    @Autowired
    private OrderInfoService orderInfoService;

    @ApiOperation(value = "提交订单")
    @PostMapping("submitOrder/{scheduleId}/{patientId}")
    public R submitOrder(@PathVariable String scheduleId, @PathVariable Long patientId) {
        Long orderId = orderInfoService.saveOrder(scheduleId, patientId);
        return R.ok().data("orderId", orderId);
    }

    @ApiOperation(value = "分页查询订单详情")
    @GetMapping("{page}/{limit}")
    public R getPageList(@PathVariable Integer page,
                         @PathVariable Integer limit,
                         OrderQueryVo orderQueryVo,
                         @RequestHeader String token) {
        Long userId = JwtHelper.getUserId(token);
        orderQueryVo.setUserId(userId);
        Page<OrderInfo> orderInfoPage = orderInfoService.getPageList(page, limit, orderQueryVo);
        return R.ok().data("pageModel", orderInfoPage);
    }

    @ApiOperation(value = "获取订单支付状态")
    @GetMapping("getStatusList")
    public R getStatusList() {
        List<Map<String, Object>> statusList = OrderStatusEnum.getStatusList();
        return R.ok().data("statusList", statusList);
    }

    @ApiOperation(value = "订单详情")
    @GetMapping("getOrders/{orderId}")
    public R getOrders(@PathVariable Long orderId, @RequestHeader String token) {
        Long userId = JwtHelper.getUserId(token);
        OrderInfo orderInfo = orderInfoService.getOrders(orderId, userId);
        return R.ok().data("orderInfo", orderInfo);
    }

    @ApiOperation(value = "取消订单")
    @GetMapping("cancelOrder/{orderId}")
    public R cancelOrder(@PathVariable Long orderId) {
        orderInfoService.cancelOrder(orderId);
        return R.ok();
    }

    @ApiOperation(value = "统计医院订单信息")
    @PostMapping("getCountMap")
    public Map<String, Object> getCountMap(@RequestBody OrderCountQueryVo orderCountQueryVo) {
        Map<String, Object> map = orderInfoService.getCountMap(orderCountQueryVo);
        return map;
    }

}

