package com.atguigu.yygh.order.service;

import java.util.Map;

/**
 * @author Xu
 * @date 2022/4/28 19:29
 * yygh_parent com.atguigu.yygh.order.service
 */
public interface WeixinService {


    String createNative(Long orderId);


    Map<String, String> queryPayStatus(Long orderId);

    boolean refund(Long orderId);
}
