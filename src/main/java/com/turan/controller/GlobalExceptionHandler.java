package com.turan.controller;

import com.turan.common.BizException;
import com.turan.common.ResultBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;

import static com.turan.common.RespCode.*;


/**
 * 全局异常捕获
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    /**
     * controller入参校验异常
     */
    @ExceptionHandler(value = BindException.class)
    @ResponseStatus(value = HttpStatus.OK)
    public Object bindExceptionHandler(BindException e) {
        log.warn("GlobalExceptionHandler catch BindException: {}", e.getMessage());
        return ResultBean.of(INVALID_PARAMS, e.getMessage());
    }

    /**
     * controller入参校验异常
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    @ResponseStatus(value = HttpStatus.OK)
    public Object methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        log.warn("GlobalExceptionHandler catch MethodArgumentNotValidException: {}", e.getMessage());
        return ResultBean.of(INVALID_PARAMS, e.getMessage());
    }

    /**
     * controller参数类型不匹配
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(value = HttpStatus.OK)
    public Object methodArgumentTypeMismatchExceptionHandler(MethodArgumentTypeMismatchException e) {
        log.warn("GlobalExceptionHandler catch MethodArgumentNotValidException: {}" , e.getMessage());
        return ResultBean.of(INVALID_PARAMS, e.getMessage());
    }

    /**
     * controller 方法不允许异常
     */
    @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
    @ResponseStatus(value = HttpStatus.OK)
    public Object methodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("GlobalExceptionHandler catch HttpRequestMethodNotSupportedException: {}", e.getMessage());
        return ResultBean.of(INVALID_PARAMS, e.getMessage());
    }

    /**
     * controller 缺少参数异常
     */
    @ExceptionHandler({MissingServletRequestParameterException.class})
    @ResponseStatus(value = HttpStatus.OK)
    public Object missingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.warn("GlobalExceptionHandler catch MissingServletRequestParameterException: {}", e.getMessage());
        return ResultBean.of(INVALID_PARAMS, e.getMessage());
    }

    /**
     * 参数不合法异常
     */
    @ExceptionHandler({IllegalArgumentException.class})
    @ResponseStatus(value = HttpStatus.OK)
    public Object illegalArgumentException(IllegalArgumentException e) {
        log.warn("GlobalExceptionHandler catch IllegalArgumentException: {}", e.getMessage());
        return ResultBean.of(INVALID_PARAMS, e.getMessage());
    }

    /**
     * http消息体非法
     */
    @ExceptionHandler({HttpMessageNotReadableException.class})
    @ResponseStatus(value = HttpStatus.OK)
    public Object handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("GlobalExceptionHandler catch HttpMessageNotReadableException: {}", e.getMessage());
        return ResultBean.of(INVALID_PARAMS, e.getMessage());
    }

    /**
     * 业务异常处理
     */
    @ExceptionHandler(value = BizException.class)
    @ResponseStatus(value = HttpStatus.OK)
    public Object exceptionHandler(BizException e, HttpServletRequest request) {
        log.warn("GlobalExceptionHandler catch BizException: url=[{}],code=[{}],msg=[{}]", request.getRequestURI(), e.getCode(), e.getMsg());
        return ResultBean.of(e.getCode(), e.getMsg());
    }

    /**
     * 运行时异常处理
     */
    @ExceptionHandler(value = Exception.class)
    public Object exceptionHandler(Exception e) {
        log.error("GlobalExceptionHandler catch unknown Exception: ", e);
        return ResultBean.of(SYS_ERROR);
    }
}
