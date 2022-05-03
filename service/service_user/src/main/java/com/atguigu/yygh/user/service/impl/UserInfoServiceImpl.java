package com.atguigu.yygh.user.service.impl;


import com.atguigu.enums.AuthStatusEnum;
import com.atguigu.model.user.Patient;
import com.atguigu.model.user.UserInfo;
import com.atguigu.vo.user.LoginVo;
import com.atguigu.vo.user.UserAuthVo;
import com.atguigu.vo.user.UserInfoQueryVo;
import com.atguigu.yygh.exception.YYGHException;
import com.atguigu.yygh.user.mapper.UserInfoMapper;
import com.atguigu.yygh.user.service.PatientService;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.utils.JwtHelper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.security.auth.message.AuthStatus;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2022-04-23
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {
    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private PatientService patientService;

    @Override
    public Map<String, Object> login(LoginVo loginVo) {
        //校验参数
        String phone = loginVo.getPhone();
        String code = loginVo.getCode();
        if (StringUtils.isEmpty(phone) || StringUtils.isEmpty(code)) {
            throw new YYGHException(20001, "账号和密码不能为空");
        }
        //todo 校验验证码
        String redisCode = redisTemplate.opsForValue().get(phone);
        if (StringUtils.isEmpty(redisCode) || !redisCode.equals(code)) {
            throw new YYGHException(20001, "验证码有误");
        }

        Map<String, Object> map = new HashMap<>();
        //判断是否为强制手机号注册
        if (StringUtils.isEmpty(loginVo.getOpenid())) {
            //openid为空 纯手机注册
            //校验是否注册用户
            LambdaQueryWrapper<UserInfo> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(UserInfo::getPhone, phone);
            UserInfo userInfo = userInfoMapper.selectOne(lambdaQueryWrapper);
            //是否是首次登录 如果没有注册用户则注册微信用户
            if (userInfo == null) {
                userInfo = new UserInfo();
                userInfo.setPhone(phone);
                userInfo.setCreateTime(new Date());
                userInfo.setStatus(1);
                userInfoMapper.insert(userInfo);
            }
            //校验状态是否被禁用
            if (userInfo.getStatus() == 0) {
                throw new YYGHException(20001, "用户已被禁用");
            }

            //返回用户数据给前端页面
            map = get(userInfo);
        } else {
            UserInfo userInfoFinal = new UserInfo();
            //此时openid不为空先根据手机查有没有用手机号注册的用户
            LambdaQueryWrapper<UserInfo> lambdaQueryWrapper1 = new LambdaQueryWrapper<>();
            lambdaQueryWrapper1.eq(UserInfo::getPhone, phone);
            UserInfo userInfoPhone = userInfoMapper.selectOne(lambdaQueryWrapper1);
            if (userInfoPhone != null) {
                BeanUtils.copyProperties(userInfoPhone, userInfoFinal);
                baseMapper.delete(lambdaQueryWrapper1);
            }
            //openid有值 微信强制绑定手机号登录
            LambdaQueryWrapper<UserInfo> lambdaQueryWrapper2 = new LambdaQueryWrapper<>();
            lambdaQueryWrapper2.eq(UserInfo::getOpenid, loginVo.getOpenid());
            UserInfo userInfoWX = userInfoMapper.selectOne(lambdaQueryWrapper2);
            userInfoFinal.setOpenid(userInfoWX.getOpenid());
            userInfoFinal.setNickName(userInfoWX.getNickName());
            userInfoFinal.setId(userInfoWX.getId());
            if (userInfoPhone == null) {
                userInfoFinal.setPhone(phone);
                userInfoFinal.setStatus(userInfoWX.getStatus());
            }
            //保存手机号
            userInfoMapper.updateById(userInfoFinal);
            //校验状态是否被禁用
            if (userInfoFinal.getStatus() == 0) {
                throw new YYGHException(20001, "用户已被禁用");
            }
            map = get(userInfoFinal);
        }
        return map;
    }

    private Map<String, Object> get(UserInfo userInfo) {
        //返回页面显示名称
        Map<String, Object> map = new HashMap<>();
        String name = userInfo.getName();
        if (StringUtils.isEmpty(name)) {
            name = userInfo.getNickName();
        }
        if (StringUtils.isEmpty(name)) {
            name = userInfo.getPhone();
        }
        map.put("name", name);
        //根据userid和name生成token字符串
        String token = JwtHelper.createToken(userInfo.getId(), name);
        map.put("token", token);
        return map;
    }

    @Override
    public UserInfo selectById(Long userId) {
        UserInfo userInfo = userInfoMapper.selectById(userId);
        userInfo.getParam().put("authStatusString", AuthStatusEnum.getStatusNameByStatus(userInfo.getAuthStatus()));
        return userInfo;
    }

    @Override
    public void userAuth(Long userId, UserAuthVo userAuthVo) {
        //根据用户id查询用户信息
        UserInfo userInfo = baseMapper.selectById(userId);
        //设置认证信息
        //认证人姓名
        userInfo.setName(userAuthVo.getName());
        //其他认证信息
        userInfo.setCertificatesType(userAuthVo.getCertificatesType());
        userInfo.setCertificatesNo(userAuthVo.getCertificatesNo());
        userInfo.setCertificatesUrl(userAuthVo.getCertificatesUrl());
        userInfo.setAuthStatus(AuthStatusEnum.AUTH_RUN.getStatus());
        //进行信息更新
        baseMapper.updateById(userInfo);
    }

    @Override
    public Page<UserInfo> getPageList(Integer page, Integer limit, UserInfoQueryVo userInfoQueryVo) {

        Integer status = userInfoQueryVo.getStatus();
        Integer authStatus = userInfoQueryVo.getAuthStatus();
        String keyword = userInfoQueryVo.getKeyword();
        String createTimeBegin = userInfoQueryVo.getCreateTimeBegin();
        String createTimeEnd = userInfoQueryVo.getCreateTimeEnd();

        Page<UserInfo> page1 = new Page<>(page,limit);
        //对条件值进行非空判断
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        if(!StringUtils.isEmpty(keyword)) {
            wrapper.like("name",keyword).or().eq("phone",keyword).or().like("nick_name",keyword);
        }
        if(!StringUtils.isEmpty(status)) {
            wrapper.eq("status",status);
        }
        if(!StringUtils.isEmpty(authStatus)) {
            wrapper.eq("auth_status",authStatus);
        }
        if(!StringUtils.isEmpty(createTimeBegin)) {
            wrapper.ge("create_time",createTimeBegin);
        }
        if(!StringUtils.isEmpty(createTimeEnd)) {
            wrapper.le("create_time",createTimeEnd);
        }
        Page<UserInfo> userInfoPage = userInfoMapper.selectPage(page1, wrapper);
      userInfoPage.getRecords().forEach(this::packageUserInfo);
        return  userInfoPage;
    }

    private void packageUserInfo(UserInfo userInfo) {
        //处理认证状态编码
        String statusString = userInfo.getStatus().intValue()==0 ?"锁定" : "正常";
        userInfo.getParam().put("statusString",statusString );
        userInfo.getParam().put("authStatusString",AuthStatusEnum.getStatusNameByStatus(userInfo.getAuthStatus()));
    }

    @Override
    public void lock(Long userId, Integer status) {
        if (status ==0 ||status==1) {
            UserInfo userInfo = new UserInfo();
            userInfo.setId(userId);
            userInfo.setStatus(status);
            userInfoMapper.updateById(userInfo);
        }
    }


    @Override
    public Map<String, Object> show(Long userId) {
        Map<String,Object> map = new HashMap<>();
        //查询用户讯息
        UserInfo userInfo = userInfoMapper.selectById(userId);
        this.packageUserInfo(userInfo);
        map.put("userInfo",userInfo);
        //查询就诊人信息
        List<Patient> patientList = patientService.findAllUserId(userId);
        map.put("patientList",patientList);
        return map;
    }


    @Override
    public void approval(Long userId, Integer authStatus) {
        if (authStatus==2||authStatus== -1){
            UserInfo userInfo = new UserInfo();
            userInfo.setId(userId);
            userInfo.setAuthStatus(authStatus);
            userInfoMapper.updateById(userInfo);
        }
    }
}
