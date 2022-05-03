package com.atguigu.yygh.user.service;


import com.atguigu.model.user.UserInfo;
import com.atguigu.vo.user.LoginVo;
import com.atguigu.vo.user.UserAuthVo;
import com.atguigu.vo.user.UserInfoQueryVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author atguigu
 * @since 2022-04-23
 */
public interface UserInfoService extends IService<UserInfo> {

    Map<String, Object> login(LoginVo loginVo);


    UserInfo selectById(Long userId);

    void userAuth(Long userId, UserAuthVo userAuthVo);

    Page<UserInfo> getPageList(Integer page, Integer limit, UserInfoQueryVo userInfoQueryVo);

    void lock(Long userId, Integer status);

    Map<String, Object> show(Long userId);

    void approval(Long userId, Integer authStatus);

}
