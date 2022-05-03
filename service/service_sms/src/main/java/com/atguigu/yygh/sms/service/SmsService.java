package com.atguigu.yygh.sms.service;

import com.atguigu.vo.msm.MsmVo;
import com.atguigu.yygh.sms.utils.HttpUtils;
import com.atguigu.yygh.sms.utils.RandomUtil;
import org.apache.http.HttpResponse;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Xu
 * @date 2022/4/23 11:19
 * yygh_parent com.atguigu.yygh.sms.service
 */
@Service
public class SmsService {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public boolean send(String phone) {
        //判断redis里的验证码是否为空  为空则发送新的验证码 Redis中有数据则不发送验证码
        String code = redisTemplate.opsForValue().get(phone);
        if (!StringUtils.isEmpty(code)) {
            return true;
        }
        String host = "http://dingxin.market.alicloudapi.com";
        String path = "/dx/sendSms";
        String method = "POST";
        String appcode = "64787a04eee44b2299fd2b2808215978";

        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);

        String fourBitRandom = RandomUtil.getFourBitRandom();
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("mobile", phone);
        querys.put("param", "code:" + fourBitRandom);
        querys.put("tpl_id", "TP1711063");

        Map<String, String> bodys = new HashMap<String, String>();

        try {
            /**
             * 重要提示如下:
             * HttpUtils请从
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
             * 下载
             *
             * 相应的依赖请参照
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
             */
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            redisTemplate.opsForValue().set(phone, fourBitRandom, 5, TimeUnit.DAYS);
            return true;
            //获取response的body
            //System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public void subMitMessage(MsmVo msmVo) {
        String phone = msmVo.getPhone();
        System.out.println(phone + "预约成功");
    }

    public void remind(MsmVo msmVo) {
        String patientTips = msmVo.getParam().toString();
        System.out.println("patientTips = " + patientTips);
    }
}
