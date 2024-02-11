package com.greenroom.server.api.exception;

import com.greenroom.server.api.utils.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.greenroom.server.api.enums.ResponseCodeEnum.FAILED;
import static com.greenroom.server.api.enums.ResponseCodeEnum.RESULT_NOT_FOUND;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            LoginException.class
    })
    public ApiResponse customException(CustomException e) {
        if (e.getResponseCodeEnum() != RESULT_NOT_FOUND) {
            log.warn("{} : {}", e.getClass(), e.getMessage());
            log.warn("{}", e.getStackTrace()[0]);
        }
        return ApiResponse.failed(e.getResponseCodeEnum()).message(e.getMessage());
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class
    })
    public ApiResponse handlingException(Exception e) {
        log.error("{} : {}", e.getClass(), e.getMessage());
        log.error("{}", e.getStackTrace()[0]);
        return ApiResponse.failed(FAILED).message(e.getMessage());
    }
}
