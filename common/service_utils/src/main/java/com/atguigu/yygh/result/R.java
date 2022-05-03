package com.atguigu.yygh.result;

import com.atguigu.yygh.enums.ResultEnum;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Xu
 * @date 2022/4/12 19:20
 * yygh_parent com.atguigu.yygh.common.result
 */

@Data
public class R {
    private Integer code;
    private boolean success;
    private String message;
    private Map<String, Object> data = new HashMap<>();



    //构造器私有化
    private R() {

    }

    //链式调用
    public static R ok() {
        R resultVO = new R();
        resultVO.setCode(ResultEnum.SUCCESS.getCode());
        resultVO.setMessage(ResultEnum.SUCCESS.getMessage());
        resultVO.setSuccess(true);
        return resultVO;
    }

    public static R error() {
        R resultVO = new R();
        resultVO.setCode(ResultEnum.ERROR.getCode());
        resultVO.setMessage(ResultEnum.ERROR.getMessage());
        resultVO.setSuccess(false);
        return resultVO;
    }

    public R code(Integer code) {
        this.setCode(code);
        return  this;
    }

    public R message(String message) {
        this.setMessage(message);
        return  this;
    }

    public R success(boolean success) {
        this.setSuccess(success);
        return  this;
    }

    public R data(Map<String, Object> map) {
        this.setData(map);
        return  this;
    }

    public R data(String key,Object value) {
        this.data.put(key,value);
        return  this;
    }
}
