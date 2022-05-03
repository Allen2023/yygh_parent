package com.atguigu.yygh.handler;

import com.atguigu.yygh.exception.YYGHException;
import com.atguigu.yygh.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;

/**
 * @author Xu
 * @date 2022/4/13 9:55
 * yygh_parent com.atguigu.yygh.handler
 */
@Slf4j
@RestControllerAdvice//全局异常处理类
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class) //局部异常处理方法
    public R handleAllException(Exception ex) {
        ex.printStackTrace();
        log.error(ex.getMessage());
        return R.error();
    }

    @ExceptionHandler(value = ArithmeticException.class)//算术异常
    public R handleArithmeticException(Exception ex) {
        ex.printStackTrace();
        // log.error( ex.getMessage());
        return R.error().message("数学异常");
    }

    @ExceptionHandler(value = SQLException.class)//SQL异常
    public R handleSQLException(Exception ex) {
        ex.printStackTrace();
        //log.error( ex.getMessage());
        return R.error().message("SQL异常");
    }

    @ExceptionHandler(value = YYGHException.class)//自定义异常
    public R handleYYGHException(YYGHException ex) {
        ex.printStackTrace();
        log.error(ex.getMessage());
        return R.error().message(ex.getMessage()).code(ex.getCode());
    }

}
