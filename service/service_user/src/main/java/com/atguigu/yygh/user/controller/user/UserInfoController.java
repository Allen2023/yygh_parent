package com.atguigu.yygh.user.controller.user;


import com.atguigu.model.user.UserInfo;
import com.atguigu.vo.user.LoginVo;
import com.atguigu.vo.user.UserAuthVo;
import com.atguigu.yygh.result.R;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.utils.JwtHelper;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author atguigu
 * @since 2022-04-23
 */
@RestController
@RequestMapping("/user/userinfo")
public class UserInfoController {

    @Autowired
    private UserInfoService userInfoService;

    @ApiOperation(value = "会员登录")
    @PostMapping("/login")
    public R login(@RequestBody LoginVo loginVo) {
        Map<String, Object> map = userInfoService.login(loginVo);
        return R.ok().data(map);
    }

    @ApiOperation(value = "获取登录用户信息")
    @GetMapping("/info")
    public R getUserInfo(@RequestHeader(value = "token") String token) {
        Long userId = JwtHelper.getUserId(token);
        UserInfo userInfo = userInfoService.selectById(userId);
        return R.ok().data("userInfo",userInfo);
    }

    //用户认证接口
    @PostMapping("/userAuth")
    public R userAuth(@RequestBody UserAuthVo userAuthVo, HttpServletRequest request) {
        //传递两个参数，第一个参数用户id，第二个参数认证数据vo对象
        String token = request.getHeader("token");
        Long userId = JwtHelper.getUserId(token);
        userInfoService.userAuth(userId,userAuthVo);
        return R.ok();
    }
}

