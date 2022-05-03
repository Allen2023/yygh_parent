package com.atguigu.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.enums.PaymentTypeEnum;
import com.atguigu.enums.RefundStatusEnum;
import com.atguigu.model.order.OrderInfo;
import com.atguigu.model.order.PaymentInfo;
import com.atguigu.model.order.RefundInfo;
import com.atguigu.yygh.exception.YYGHException;
import com.atguigu.yygh.order.prop.ConstantPropertiesUtils;
import com.atguigu.yygh.order.service.OrderInfoService;
import com.atguigu.yygh.order.service.PaymentService;
import com.atguigu.yygh.order.service.RefundInfoService;
import com.atguigu.yygh.order.service.WeixinService;
import com.atguigu.yygh.order.utils.HttpClient;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Xu
 * @date 2022/4/28 19:29
 * yygh_parent com.atguigu.yygh.order.service.impl
 */
@Service
public class WeixinServiceImpl implements WeixinService {

    @Autowired
    private WeixinService weixinService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OrderInfoService orderInfoService;

    @Autowired
    private RefundInfoService refundInfoService;


    /**
     * 根据订单号下单，生成支付链接
     */
    @Override
    public String createNative(Long orderId) {
        try {
            //根据订单id获取订单信息
            OrderInfo orderInfo = orderInfoService.getById(orderId);
            //保存支付记录
            paymentService.savePaymentInfo(orderInfo, PaymentTypeEnum.WEIXIN.getStatus());
            //请求微信服务器
            //1、设置参数
            Map paramMap = new HashMap();
            paramMap.put("appid", ConstantPropertiesUtils.APPID);
            paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);

            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            Date reserveDate = orderInfo.getReserveDate();
            String reserveDateString = new DateTime(reserveDate).toString("yyyy/MM/dd");
            String body = reserveDateString + "就诊" + orderInfo.getDepname();
            paramMap.put("body", body);
            paramMap.put("out_trade_no", orderInfo.getOutTradeNo());
            //paramMap.put("total_fee", orderInfo.getAmount().multiply(new BigDecimal("100")).longValue()+"");
            paramMap.put("total_fee", "1");//为了测试
            paramMap.put("spbill_create_ip", "127.0.0.1");

            paramMap.put("notify_url", "http://guli.shop/api/order/weixinPay/weixinNotify");
            paramMap.put("trade_type", "NATIVE");
            //2、HTTPClient来根据URL访问第三方接口并且传递参数
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            //client设置参数
            client.setXmlParam(WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY));
            client.setHttps(true);
            client.post();
            //3.返回第三方数据
            String xml = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            //4、封装返回结果集
            //返回url
            return resultMap.get("code_url");
        } catch (Exception e) {
            e.printStackTrace();
            throw new YYGHException(20001, "获取二维码地址失败");
        }
    }

    @Override
    public Map<String, String> queryPayStatus(Long orderId) {
        try {
            //根据订单id获取订单信息
            OrderInfo orderInfo = orderInfoService.getById(orderId);
            Map map = new HashMap<String, String>();
            map.put("appid", ConstantPropertiesUtils.APPID);
            map.put("mch_id", ConstantPropertiesUtils.PARTNER);
            map.put("out_trade_no", orderInfo.getOutTradeNo());
            map.put("nonce_str", WXPayUtil.generateNonceStr());
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            client.setXmlParam(WXPayUtil.generateSignedXml(map, ConstantPropertiesUtils.PARTNERKEY));
            client.setHttps(true);
            client.post();
            String content = client.getContent();
            Map<String, String> stringMap = WXPayUtil.xmlToMap(content);
            return stringMap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean refund(Long orderId) {
        try {
            PaymentInfo paymentInfo = paymentService.getOne(new LambdaQueryWrapper<PaymentInfo>().eq(PaymentInfo::getOrderId, orderId));
            RefundInfo refundInfo = refundInfoService.saveRefundInfo(paymentInfo);
            if (refundInfo.getRefundStatus().intValue() == RefundStatusEnum.REFUND.getStatus().intValue()) {
                return true;
            }
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("appid", ConstantPropertiesUtils.APPID);       //公众账号ID
            paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);   //商户编号
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            paramMap.put("transaction_id", paymentInfo.getTradeNo()); //微信订单号
            paramMap.put("out_trade_no", paymentInfo.getOutTradeNo()); //商户订单编号
            paramMap.put("out_refund_no", "tk" + paymentInfo.getOutTradeNo()); //商户退款单号
            paramMap.put("total_fee", "1");
            paramMap.put("refund_fee", "1");
            String paramXml = WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY);
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/secapi/pay/refund");
            client.setXmlParam(paramXml);
            client.setHttps(true);
            client.setCert(true);
            client.setCertPassword(ConstantPropertiesUtils.PARTNER);
            client.post();
            //3、返回第三方的数据
            String content = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(content);
            if (null != resultMap && WXPayConstants.SUCCESS.equalsIgnoreCase(resultMap.get("result_code"))) {
                refundInfo.setCallbackTime(new Date());
                refundInfo.setTradeNo(resultMap.get("refund_id"));
                refundInfo.setRefundStatus(RefundStatusEnum.REFUND.getStatus());
                refundInfo.setCallbackContent(JSONObject.toJSONString(resultMap));
                refundInfoService.updateById(refundInfo);
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }
}
