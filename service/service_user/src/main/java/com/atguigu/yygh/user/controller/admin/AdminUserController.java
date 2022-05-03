package com.atguigu.yygh.user.controller.admin;

import com.atguigu.model.user.UserInfo;
import com.atguigu.vo.user.UserInfoQueryVo;
import com.atguigu.yygh.result.R;
import com.atguigu.yygh.user.service.UserInfoService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Xu
 * @date 2022/4/26 13:42
 * yygh_parent com.atguigu.yygh.user.controller.admin
 */
@RestController
@RequestMapping("/admin/userinfo")
public class AdminUserController {

    @Autowired
    private UserInfoService userInfoService;

    @ApiOperation(value = "分页查询用户管理")
    @GetMapping("/{page}/{limit}")
    public R  getPageList(@PathVariable Integer page, @PathVariable Integer limit, UserInfoQueryVo userInfoQueryVo){
           Page<UserInfo> pageResult = userInfoService.getPageList(page,limit,userInfoQueryVo);
             return R.ok().data("page",pageResult);
    }

    @ApiOperation(value = "锁定")
    @GetMapping("lock/{userId}/{status}")
    public R  lock(@PathVariable Long userId, @PathVariable Integer status){
        userInfoService.lock(userId,status);
        return R.ok();
    }


    @ApiOperation(value = "用户详情")
    @GetMapping("/show/{userId}")
    public R  show(@PathVariable Long userId){
        Map<String,Object> map  = userInfoService.show(userId);
        return R.ok().data(map);
    }


    @ApiOperation(value = "认证审批")
    @GetMapping("approval/{userId}/{authStatus}")
    public R  approval(@PathVariable Long userId,@PathVariable Integer authStatus ){
         userInfoService.approval(userId,authStatus);
        return R.ok();
    }
}
