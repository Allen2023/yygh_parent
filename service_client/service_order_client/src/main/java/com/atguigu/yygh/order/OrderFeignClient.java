package com.atguigu.yygh.order;

import com.atguigu.vo.order.OrderCountQueryVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @author Xu
 * @date 2022/5/1 16:55
 * yygh_parent com.atguigu.yygh.order
 */
@FeignClient("service-orders")
public interface OrderFeignClient {

    @ApiOperation(value = "统计医院订单信息")
    @PostMapping("/api/orderInfo/getCountMap")
    public Map<String, Object> getCountMap(@RequestBody OrderCountQueryVo orderCountQueryVo);
}
