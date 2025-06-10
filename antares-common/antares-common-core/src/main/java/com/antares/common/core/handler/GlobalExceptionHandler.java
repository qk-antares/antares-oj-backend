package com.antares.common.core.handler;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

import com.antares.common.core.dto.R;
import com.antares.common.core.enums.HttpCodeEnum;
import com.antares.common.core.exception.BusinessException;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice   // @ControllerAdvice + @ResponseBody
@Slf4j
public class GlobalExceptionHandler {
ExceptionHandlerExceptionResolver exceptionHandlerExceptionResolver;

    @ExceptionHandler(BusinessException.class)
    public R<Void> handleBusinessException(BusinessException e){
        log.info("出现异常: [{}], 原因: [{}]", e.getClass().getName(), e.getMessage());
        return R.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public R<Void> handleValidationException(ConstraintViolationException e){
        for(ConstraintViolation<?> s : e.getConstraintViolations()){
            log.info("出现异常: [{}]，原因: [{}, ({}) {}]", e.getClass().getName(), HttpCodeEnum.BAD_REQUEST.getMsg(), s.getPropertyPath(), s.getMessage());
            return R.error(HttpCodeEnum.BAD_REQUEST.getCode(), s.getInvalidValue()+": "+s.getMessage());
        }
        return R.error(HttpCodeEnum.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleValidationException(MethodArgumentNotValidException e){
        for(FieldError s : e.getBindingResult().getFieldErrors()){
            log.info("出现异常: [{}]，原因: [{}, ({}) {}]", e.getClass().getName(), HttpCodeEnum.BAD_REQUEST.getMsg(), s.getField(), s.getDefaultMessage());
            return R.error(HttpCodeEnum.BAD_REQUEST.getCode(), s.getField()+": "+s.getDefaultMessage());
        }
        return R.error(HttpCodeEnum.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e){
        log.error("服务器内部异常：[{}]，原因：[{}]", e.getClass().getName(), e.getMessage());
        return R.error(HttpCodeEnum.INTERNAL_SERVER_ERROR);
    }
}
