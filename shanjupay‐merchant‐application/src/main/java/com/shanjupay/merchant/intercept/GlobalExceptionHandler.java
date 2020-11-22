package com.shanjupay.merchant.intercept;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.domain.ErrorCode;
import com.shanjupay.common.domain.RestErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @title: GlobalExceptionHandler
 * @Author Tang Xiaojiang
 * @Date: 2020/11/21 16:33
 * @Version 1.0
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    private final static Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 捕获异常后处理方法
     *
     * @param request
     * @param response
     * @param e
     * @return
     */
    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse processException(HttpServletRequest request, HttpServletResponse response, Exception e) {
        // 如果时自定义的异常，则直接取出信息errCode和errMessage
        if (e instanceof BusinessException) {
            LOGGER.info(e.getMessage(), e);
            // 解析系统自定义异常信息
            BusinessException businessException = (BusinessException) e;
            ErrorCode errorCode = businessException.getErrorCode();
            // 错误代码
            String code = String.valueOf(errorCode.getCode());
            // 错误信息
            String desc = errorCode.getDesc();
            return new RestErrorResponse(code, desc);
        }
        LOGGER.error("系统异常：", e);
        // 统一定义为99999系统未知异常
        return new RestErrorResponse(CommonErrorCode.UNKNOWN.getDesc(), String.valueOf(CommonErrorCode.UNKNOWN.getCode()));
    }
}
