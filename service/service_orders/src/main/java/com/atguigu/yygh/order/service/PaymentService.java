package com.atguigu.yygh.order.service;

import com.atguigu.model.order.OrderInfo;
import com.atguigu.model.order.PaymentInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

public interface PaymentService extends IService<PaymentInfo> {
    /**
     * 保存交易记录
     * @param order
     * @param paymentType 支付类型（1：微信 2：支付宝）
     */
    void savePaymentInfo(OrderInfo order, Integer paymentType);

    void updatePay(Long orderId, Map<String, String> map);

}