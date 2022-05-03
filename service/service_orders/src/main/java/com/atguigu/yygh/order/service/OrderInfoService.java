package com.atguigu.yygh.order.service;


import com.atguigu.model.order.OrderInfo;
import com.atguigu.vo.order.OrderCountQueryVo;
import com.atguigu.vo.order.OrderQueryVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 * 订单表 服务类
 * </p>
 *
 * @author atguigu
 * @since 2022-04-27
 */
public interface OrderInfoService extends IService<OrderInfo> {

    Long saveOrder(String scheduleId, Long patientId);


    Page<OrderInfo> getPageList(Integer page, Integer limit, OrderQueryVo orderQueryVo);

    OrderInfo getOrders(Long orderId, Long userId);

    void cancelOrder(Long orderId);

    void remind();

    Map<String, Object> getCountMap(OrderCountQueryVo orderCountQueryVo);

}
