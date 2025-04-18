package com.antares.common.core.handler;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.antares.common.core.dto.R;
import com.antares.common.core.enums.HttpCodeEnum;
import com.antares.common.core.exception.BusinessException;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
@RestController
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    @ResponseBody
    public R<Void> systemExceptionHandler(BusinessException e){
        //打印异常信息
        log.error("出现异常：[{}]，原因：[{}]", e.getClass().getName(), e.getMessage());
        //从异常对象中获取信息，封装成ResponseResult后返回
        return R.error(e.getCode(), e.getMsg());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    public R<Void> handleValidationException(ConstraintViolationException e){
        //打印异常信息
        log.error("出现异常：[{}]，原因：[{}]", e.getClass().getName(), "请求参数不合法");
        for(ConstraintViolation<?> s : e.getConstraintViolations()){
            return R.error(HttpCodeEnum.BAD_REQUEST.getCode(), s.getInvalidValue()+": "+s.getMessage());
        }
        return R.error(HttpCodeEnum.BAD_REQUEST.getCode(), "请求参数不合法");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public R<Void> handleValidationException(MethodArgumentNotValidException e){
        //打印异常信息
        log.error("出现异常：[{}]，原因：[{}]", e.getClass().getName(), "请求参数不合法");
        for(FieldError s : e.getBindingResult().getFieldErrors()){
            return R.error(HttpCodeEnum.BAD_REQUEST.getCode(), s.getField()+": "+s.getDefaultMessage());
        }
        return R.error(HttpCodeEnum.BAD_REQUEST.getCode(), "请求参数不合法");
    }

    @ExceptionHandler(Exception.class)
    public R<Void> exceptionHandler(Exception e){
        //打印异常信息
        log.error("出现未知异常！", e);
        //从异常对象中获取信息，封装成ResponseResult后返回
        return R.error(HttpCodeEnum.INTERNAL_SERVER_ERROR.getCode(), e.getMessage());
    }
}
