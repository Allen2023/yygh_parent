package com.atguigu.yygh.user.controller.user;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.model.user.UserInfo;
import com.atguigu.yygh.exception.YYGHException;
import com.atguigu.yygh.result.R;
import com.atguigu.yygh.user.prop.WXProperties;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.user.utils.HttpClientUtils;
import com.atguigu.yygh.utils.JwtHelper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Xu
 * @date 2022/4/23 19:17
 * yygh_parent com.atguigu.yygh.user.controller
 */
@Controller
@RequestMapping("/api/userinfo/wx")
public class WxController {

    @Autowired
    private WXProperties wxProperties;

    @Autowired
    private UserInfoService userInfoService;


    @GetMapping("/getLoginParam")
    @ResponseBody
    public R genQrConnect() {
        Map<String, Object> map = new HashMap<>();
        map.put("appid", wxProperties.getAppId());
        map.put("scope", "snsapi_login");
        map.put("redirectUri", wxProperties.getRedirectUrl());
        map.put("state", System.currentTimeMillis() + "");
        map.put("style", "black");
        return R.ok().data(map);
    }

    @GetMapping("/callback")
    public String callback(String code, String state) throws Exception {
        //微信返回code和state值
        System.out.println("code = " + code);
        System.out.println("state = " + state);

        //在向微信固定地址用HTTPClient发送code,appid和appscrect
        StringBuffer baseAccessUrl = new StringBuffer()
                .append("https://api.weixin.qq.com/sns/oauth2/access_token")
                .append("?appid=%s")
                .append("&secret=%s")
                .append("&code=%s")
                .append("&grant_type=authorization_code");
        String accessTokenUrl = String.format(baseAccessUrl.toString(), wxProperties.getAppId(), wxProperties.getAppSecret(), code);
        //微信返回access_token和openid
        String accesstokenInfo = HttpClientUtils.get(accessTokenUrl);
        JSONObject jsonObject = JSONObject.parseObject(accesstokenInfo);
        String access_token = jsonObject.getString("access_token");
        String openid = jsonObject.getString("openid");
        //判断数据库中是否有该扫描人
        LambdaQueryWrapper<UserInfo> userInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //根据openid查
        userInfoLambdaQueryWrapper.eq(UserInfo::getOpenid, openid);
        UserInfo userInfo = userInfoService.getOne(userInfoLambdaQueryWrapper);

        if (userInfo == null) {//首次登陆 注册用户
            userInfo = new UserInfo();
            //向微信固定地址发送请求access_token openid 获取用户信息
            String baseUserInfoUrl = "https://api.weixin.qq.com/sns/userinfo" +
                    "?access_token=%s" +
                    "&openid=%s";
            String userInfoUrl = String.format(baseUserInfoUrl, access_token, openid);
            String resultInfo = HttpClientUtils.get(userInfoUrl);
            JSONObject resultUserInfoJson = JSONObject.parseObject(resultInfo);
            //获取用户名
            String nickname = resultUserInfoJson.getString("nickname");
            String headimgurl = resultUserInfoJson.getString("headimgurl");
            System.out.println("headimgurl = " + headimgurl);
            userInfo.setOpenid(openid);
            userInfo.setStatus(1);
            userInfo.setNickName(nickname);
            userInfoService.save(userInfo);
        }
        //校验状态是否被禁用
        if (userInfo.getStatus() == 0) {
            throw new YYGHException(20001, "用户已被禁用");
        }
        //强制绑定手机号
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isEmpty(userInfo.getPhone())) {
            //首次登录
            //如果没有绑定手机号为空,则返回openid
            map.put("openid", openid);
        } else {
            //不是首次登录
            //如果已经绑定手机号不为空,则openid为空
            map.put("openid", "");
        }


        String name = userInfo.getName();
        if (StringUtils.isEmpty(name)) {
            name = userInfo.getNickName();
        }
        if (StringUtils.isEmpty(name)) {
            name = userInfo.getPhone();
        }


        map.put("name", name);

        String token = JwtHelper.createToken(userInfo.getId(), userInfo.getName());
        map.put("token", token);

        //跳转到前端页面
        return "redirect:http://localhost:3000/weixin/callback?token=" + map.get("token") + "&openid=" + map.get("openid") + "&name=" + URLEncoder.encode((String) map.get("name"), "utf-8");
    }

}
