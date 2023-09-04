package com.antares.member.handler;

import lombok.extern.slf4j.Slf4j;
import com.antares.common.exception.BusinessException;
import com.antares.common.model.enums.AppHttpCodeEnum;
import com.antares.common.model.response.R;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    @ResponseBody
    public R systemExceptionHandler(BusinessException e){
        //打印异常信息
        log.error("出现了异常！{}", e);
        //从异常对象中获取信息，封装成ResponseResult后返回
        return R.error(e.getCode(), e.getMsg());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleValidationException(ConstraintViolationException e){
        for(ConstraintViolation<?> s:e.getConstraintViolations()){
            return s.getInvalidValue()+": "+s.getMessage();
        }
        return "请求参数不合法";
    }

    @ExceptionHandler(Exception.class)
    public R exceptionHandler(Exception e){
        //打印异常信息
        log.error("出现了异常！{}", e);
        //从异常对象中获取信息，封装成ResponseResult后返回
        return R.error(AppHttpCodeEnum.INTERNAL_SERVER_ERROR.getCode(), e.getMessage());
    }
}
