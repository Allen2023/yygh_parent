package com.atguigu.yygh.enums;

/**
 * @author Xu
 * @date 2022/4/12 19:24
 * yygh_parent com.atguigu.yygh.common.enums
 */
public enum ResultEnum {
    SUCCESS(20000,"成功"),
    ERROR(20001,"失败");


    private Integer code;
    private String message;

    ResultEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
