package com.franz.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理
 */
//@ControllerAdvice(annotations = {RestController.class, Controller.class})
//@ResponseBody
@RestControllerAdvice(annotations = {Controller.class})//这个注解相当于上面两个
@Slf4j
public class GlobalExceptionHandler {

    /**
     * SQL异常处理方法
     * @param e
     * @return
     */

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result<String> exceptionHandler(SQLIntegrityConstraintViolationException e) {
        log.error("捕获异常信息：{}", e.getMessage());

        //获取重复名，返回重复添加错误
        if(e.getMessage().contains("Duplicate entry")) {
            String name = e.getMessage().split(" ")[2];
            return Result.error("重复添加，" + name + "已存在");
        }

        return Result.error("其他未知错误");
    }

    /**
     * 自定义业务异常处理方法
     * @param e
     * @return
     */

    @ExceptionHandler(CustomException.class)
    public Result<String> exceptionHandler(CustomException e) {
        log.error("捕获异常信息：{}", e.getMessage());

        return Result.error(e.getMessage());
    }

    /**
     * 其他异常处理方法
     * @param e
     * @return
     */

    @ExceptionHandler(FileNotFoundException.class)
    public void exceptionHandler(Exception e) {
        log.error("捕获异常信息：{}", e.getMessage());
    }
}
