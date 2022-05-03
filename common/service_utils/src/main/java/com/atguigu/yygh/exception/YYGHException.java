package com.atguigu.yygh.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Xu
 * @date 2022/4/13 10:01
 * yygh_parent com.atguigu.yygh.exception
 */
@AllArgsConstructor
@Data
public class YYGHException extends RuntimeException{
    private Integer code;
    private  String message;
}
