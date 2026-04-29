package com.xuenai.medical.exception;

import com.xuenai.medical.common.BaseResponse;
import com.xuenai.medical.common.ResultUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class, HttpMessageNotReadableException.class})
    public BaseResponse<?> paramsExceptionHandler(Exception e) {
        return ResultUtils.error(ErrorCode.PARAMS_ERROR, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public BaseResponse<?> exceptionHandler(Exception e, HttpServletRequest request) {
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统异常：" + e.getMessage());
    }
}

