package com.atguigu.yygh.sta.controller;

import com.atguigu.vo.order.OrderCountQueryVo;
import com.atguigu.yygh.order.OrderFeignClient;
import com.atguigu.yygh.result.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author Xu
 * @date 2022/5/1 16:03
 * yygh_parent com.atguigu.yygh.sta.controller
 */

@Api(tags = "统计管理接口")
@RestController
@RequestMapping("/admin/sta")
public class StatisticsController {
    @Autowired
    private OrderFeignClient orderFeignClient;

    @ApiOperation(value = "获取订单统计数据")
    @GetMapping("/getCountMap")
    public R getCountMap(OrderCountQueryVo orderCountQueryVo){
        Map<String, Object> map = orderFeignClient.getCountMap(orderCountQueryVo);
        return R.ok().data(map);
    }
}
